package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Account")
public class DeleteAccountByIdEndpoint {

  public static final String PATH ="/api/account/{accountId}";

  private final AccountService accountService;

  public DeleteAccountByIdEndpoint(AccountService accountService) {
    this.accountService = accountService;
  }

  @DeleteMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_account.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Deletes an account by its ID.",
      description = "Deletes an account by its ID. Requires `account.full` or `admin.full` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "204", description = "No Content - Account deleted successfully"),
          @ApiResponse(responseCode = "404", description = "Not Found - Account does not exist",
              content = {@io.swagger.v3.oas.annotations.media.Content(
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
      }
  )
  public ResponseEntity<?> deleteAccount(@PathVariable Long accountId) {
    try {
      log.info("Attempting to delete account with ID: {}", accountId);
      accountService.deleteById(accountId);
      log.info("Account with ID {} deleted successfully.", accountId);
      return ResponseEntity.noContent().build();
    } catch (NoSuchElementException e) {
      log.error("Account with ID {} not found.", accountId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ErrorMessage());
    } catch (Exception e) {
      log.error("Error occurred while deleting account with ID {}: {}", accountId, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorMessage());
    }
  }
}
