package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.dto.RequirementDto;
import com.cairnfg.waypoint.authorization.dto.Status;
import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.CompletePasswordResetDto;
import com.cairnfg.waypoint.authorization.endpoints.oauth2.dto.SuccessfulLoginResponseDto;
import com.cairnfg.waypoint.authorization.endpoints.oauth2.service.OAuth2Service;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Authorization;
import com.cairnfg.waypoint.authorization.entity.Role;
import com.cairnfg.waypoint.authorization.service.AccountService;
import com.cairnfg.waypoint.authorization.utility.PasswordUtility;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Account")
public class CompletePasswordResetEndpoint {

  public static final String PATH = "/api/account/password/reset";
  private final PasswordEncoder passwordEncoder;
  private final AccountService accountService;
  private final OAuth2Service oAuth2Service;

  public CompletePasswordResetEndpoint(PasswordEncoder passwordEncoder,
      AccountService accountService, OAuth2Service oAuth2Service) {
    this.passwordEncoder = passwordEncoder;
    this.accountService = accountService;
    this.oAuth2Service = oAuth2Service;
  }

  @PostMapping(PATH)
  @PreAuthorize("permitAll()")
  @Operation(
      summary = "Completes a user's password reset utilizing the provided token.",
      description = "Completes a user's password reset utilizing the provided token.",
      responses = {
          @ApiResponse(responseCode = "200",
              content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = SuccessfulLoginResponseDto.class))}),
          @ApiResponse(responseCode = "400", description = "Bad Request",
              content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "404", description = "Not Found",
              content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "422", description = "Unprocessable Entity",
              content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorMessage.class))})})
  public ResponseEntity<?> completePasswordReset(
      @RequestBody CompletePasswordResetDto resetPasswordRequestDto)
      throws JOSEException {
    log.info("Attempting to update the password of account [{}]",
        resetPasswordRequestDto.getUsername());

    Optional<Account> accountOptional;

    if ((accountOptional = accountService.findByUsername(
        resetPasswordRequestDto.getUsername())).isEmpty()) {
      return generateFailureResponse(
          "Account with username " + resetPasswordRequestDto.getUsername() + " does not exist",
          HttpStatus.NOT_FOUND);
    } else if (!passwordEncoder.matches(resetPasswordRequestDto.getPasswordResetToken(),
        accountOptional.get()
            .getPasswordResetToken())) {
      return generateFailureResponse(
          "Provided passwordResetToken is invalid",
          HttpStatus.UNPROCESSABLE_ENTITY);
    } else if (PasswordUtility.validatePassword(resetPasswordRequestDto.getNewPassword())
        .getOverallStatus()
        .equals(Status.FAILED)) {
      return generateFailureResponse(
          getPasswordComplexityViolations(resetPasswordRequestDto.getNewPassword()),
          HttpStatus.BAD_REQUEST);
    } else if (LocalDateTime.now().minusMinutes(30L)
        .isAfter(accountOptional.get().getPasswordResetTimestamp())) {
      return generateFailureResponse(
          "Password Reset Token has Expired. Please try again.",
          HttpStatus.BAD_REQUEST);
    } else {
      Account accountToUpdate = accountOptional.get();

      accountToUpdate.setPassword(passwordEncoder.encode(resetPasswordRequestDto.getNewPassword()));
      accountToUpdate.setPasswordResetToken(null);
      accountToUpdate.setPasswordResetTimestamp(null);

      accountToUpdate = this.accountService.saveAccount(accountToUpdate);

      Date expiresAt = Date.from(Instant.now().plusSeconds(3600));
      Authorization authorization = oAuth2Service.generateOAuth2TokenFamily(accountToUpdate,
          expiresAt);

      log.info("Password reset for account [{}] successful", accountToUpdate.getUsername());

      return generateSuccessResponse(authorization, expiresAt);
    }
  }

  private ResponseEntity<SuccessfulLoginResponseDto> generateSuccessResponse(
      Authorization authorization, Date expiresAt) {
    SuccessfulLoginResponseDto responseDto = SuccessfulLoginResponseDto.builder()
        .accountId(authorization.getAccount().getId())
        .accessToken(authorization.getAccessToken())
        .refreshToken(authorization.getRefreshToken())
        .idToken(authorization.getIdToken())
        .expiresIn(expiresAt.getTime() - new Date().getTime())
        .permissions(authorization.getAccount()
            .getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList())
        .householdId(authorization.getAccount().getHousehold() != null ?
            authorization.getAccount().getHousehold().getId() : null)
        .roles(authorization.getAccount().getRoles().stream()
            .map(Role::getName)
            .toList())
        .build();

    return ResponseEntity.ok(responseDto);
  }

  private String getPasswordComplexityViolations(String password) {
    return PasswordUtility.validatePassword(password).getRequirements()
        .stream()
        .filter(requirementDto -> requirementDto.getStatus().equals(Status.FAILED))
        .map(RequirementDto::getMessage).collect(Collectors.joining(", "));
  }

  private ResponseEntity<ErrorMessage> generateFailureResponse(String message, HttpStatus status) {
    log.warn(message);
    return new ResponseEntity<>(
        ErrorMessage.builder()
            .path(PATH)
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(message)
            .build(),
        status
    );
  }
}
