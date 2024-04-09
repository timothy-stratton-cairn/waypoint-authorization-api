package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.dto.RequirementDto;
import com.cairnfg.waypoint.authorization.dto.Status;
import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AccountDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.PasswordResetDto;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.service.AccountService;
import com.cairnfg.waypoint.authorization.utility.PasswordUtility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Account")
public class ResetAccountPasswordEndpoint {

  public static final String PATH = "/api/account/{accountId}/reset-password";

  private final AccountService accountService;
  private final PasswordEncoder passwordEncoder;

  public ResetAccountPasswordEndpoint(AccountService accountService,
      PasswordEncoder passwordEncoder) {
    this.accountService = accountService;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_account.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Reset an account's password with the provided new password.",
      description = "Reset an account's password with the provided new password. Requires the `account.full` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = AccountDetailsDto.class))}),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))}),
          @ApiResponse(responseCode = "403", description = "Forbidden",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "404", description = "Not Found",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "422", description = "Unprocessable Entity",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))})})
  public ResponseEntity<?> getAccountById(@PathVariable Long accountId,
      @RequestBody PasswordResetDto passwordResetDto, Principal principal) {
    Optional<Account> accountToBeUpdated;

    if ((accountToBeUpdated = this.accountService.getAccountById(accountId)).isEmpty()) {
      return generateFailureResponse("Account with ID [" + accountId + "] not found",
          HttpStatus.NOT_FOUND);
    } else {
      Account validAccount = accountToBeUpdated.get();

      if (!passwordEncoder.matches(passwordResetDto.getOldPassword(), validAccount.getPassword())) {
        return generateFailureResponse(
            "Provided Old Password does not Match Old Password on Account",
            HttpStatus.UNPROCESSABLE_ENTITY);
      }

      if (PasswordUtility.validatePassword(passwordResetDto.getNewPassword()).getOverallStatus()
          .equals(Status.FAILED)) {
        return generateFailureResponse(
            getPasswordComplexityViolations(passwordResetDto.getNewPassword()),
            HttpStatus.BAD_REQUEST);
      }

      validAccount.setModifiedBy(principal.getName());
      validAccount.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));

      this.accountService.saveAccount(validAccount);

      return ResponseEntity.status(HttpStatus.OK)
          .body("Account [" + validAccount.getUsername()
              + "] password was updated successfully");
    }

  }

  private String getPasswordComplexityViolations(String password) {
    return PasswordUtility.validatePassword(password).getRequirements()
        .stream()
        .filter(requirementDto -> requirementDto.getStatus().equals(Status.FAILED))
        .map(RequirementDto::getMessage).collect(Collectors.joining(", "));
  }

  public ResponseEntity<ErrorMessage> generateFailureResponse(String message, HttpStatus status) {
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
