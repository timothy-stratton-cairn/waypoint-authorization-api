package com.cairnfg.waypoint.authorization.endpoints.oauth2;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.oauth2.dto.UserInfoDto;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "OAuth2")
@SuppressWarnings({"squid:S1075", "squid:S1452"})
public class GetUserInfoEndpoint {

  public static final String PATH = "/api/oauth/me";

  private final AccountService accountService;
  private final UserInfoDto.UserInfoDtoMapper mapper = UserInfoDto.UserInfoDtoMapper.INSTANCE;

  public GetUserInfoEndpoint(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping(PATH)
  @PreAuthorize("hasAuthority('SCOPE_account.read')")
  @Operation(
      summary = "Retrieves information around the currently logged in user.",
      description = "Retrieves information around the currently logged in user. Requires the `account.read` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = UserInfoDto.class))}),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))}),
          @ApiResponse(responseCode = "403", description = "Forbidden",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "404", description = "Not Found",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))})})
  public ResponseEntity<?> getUserInfo(Principal principal) {
    log.info("Retrieving user account with username [{}]", principal.getName());
    final ResponseEntity<?>[] response = new ResponseEntity<?>[1];

    accountService.findByUsername(principal.getName())
        .ifPresentOrElse(
            returnedAccount -> response[0] = generateSuccessResponse(returnedAccount),
            () -> response[0] = generateFailureResponse(principal.getName())
        );

    return response[0];
  }

  public ResponseEntity<UserInfoDto> generateSuccessResponse(Account account) {
    return ResponseEntity.ok(mapper.accountToUserInfoDto(account));
  }

  public ResponseEntity<ErrorMessage> generateFailureResponse(String username) {
    log.warn("Account with username [{}] not found", username);
    return new ResponseEntity<>(
        ErrorMessage.builder()
            .path(PATH)
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Account with username [" + username + "] not found")
            .build(),
        HttpStatus.NOT_FOUND
    );
  }
}
