package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.service.AccountService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Account")
public class ResetPasswordEndpoint {

  public static final String PATH = "/api/account/password/reset";
  private final AccountService accountService;

  public ResetPasswordEndpoint(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping(PATH)
  @PreAuthorize("permitAll()")
  @Operation(
      summary = "Sends a user an email to reset their password.",
      description =
          "Sends a user an email to reset their password.",
      responses = {
          @ApiResponse(responseCode = "200",
              content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                  schema = @Schema(implementation = String.class))})})
  public ResponseEntity<String> resetPassword(@RequestParam Optional<String> username,
      @RequestParam Optional<String> email)
      throws JsonProcessingException {
    log.info("Attempting to reset password for account with username/email [{}/{}}", username,
        email);

    try {
      Optional<Account> userAccountOptional = username.isPresent() ?
          accountService.findByUsername(username.get()) :
          accountService.findByEmail(email
              .orElseThrow(EntityNotFoundException::new));

      accountService.resetPassword(userAccountOptional.orElseThrow(EntityNotFoundException::new));

      log.info("Password Reset for account [{}] successful",
          userAccountOptional.get().getUsername());
    } catch (EntityNotFoundException e) {
      log.warn("Password Reset for account [{}/{}] unsuccessful. Account not found.",
          username, email, e);
    }
    return ResponseEntity.ok("If an account associated with username/email ["
        + username + "/" + email
        + "] exists, a password reset link will be sent to the email associated with the account");
  }
}
