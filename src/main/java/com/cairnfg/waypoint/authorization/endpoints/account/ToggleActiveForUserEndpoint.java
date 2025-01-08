package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Household;
import com.cairnfg.waypoint.authorization.service.AccountService;
import com.cairnfg.waypoint.authorization.service.HouseholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Account")
public class ToggleActiveForUserEndpoint {

  public static final String PATH = "/api/account/toggle-active/{accountId}";
  private final AccountService accountService;
  private final HouseholdService householdService;

  public ToggleActiveForUserEndpoint(AccountService accountService,
      HouseholdService householdService) {
    this.accountService = accountService;
    this.householdService = householdService;
  }

  @Transactional
  @PatchMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_household.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Toggles the active state of a household.",
      description = "Toggles the active state of a household identified by householdId. Requires the `household.full` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              description = "OK - Household active state toggled successfully"),
          @ApiResponse(responseCode = "400", description = "Bad Request",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))}),
          @ApiResponse(responseCode = "403", description = "Forbidden",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "404", description = "Not Found",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))})
      })
  public ResponseEntity<?> toggleActiveAccount(@PathVariable Long accountId) {
    try {
      Optional<Account> accountOptional = accountService.getAccountById(accountId);

      if (accountOptional.isEmpty()) {
        log.warn("Account with ID {} not found", accountId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorMessage());
      }
        int numOfAccountsLeftInHousehold = accountService.getAccountsByHouseholdId(
            accountOptional.get().getHousehold().getId()).size();
        if (numOfAccountsLeftInHousehold == 1 ) {
          Optional<Household> householdOptional = householdService.getHouseholdById(
              accountOptional.get().getHousehold().getId());

          householdOptional.ifPresent(household -> {
            household.setActive(false);
            householdService.saveHousehold(household);
            log.info("Household with ID {} set to inactive as the last user was deactivated",
                household.getId());
          });
        }

      Account account = accountOptional.get();
      boolean newState = !Boolean.TRUE.equals(account.getActive());
      account.setActive(newState);
      accountService.saveAccount(account);

      log.info("Account with ID {} toggled to active state: {}", accountId, newState);
      return ResponseEntity.ok("Account active state toggled successfully to: " + newState);
    } catch (Exception e) {
      log.error("Error toggling active state for Account with ID {}: {}", accountId, e.getMessage(),
          e);
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorMessage());
    }

  }

}
