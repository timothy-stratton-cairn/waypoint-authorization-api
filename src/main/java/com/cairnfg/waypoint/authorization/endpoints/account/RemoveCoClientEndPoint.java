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
import java.security.Principal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Account")
public class RemoveCoClientEndPoint {

  public static final String PATH = "/api/account/remove_coclient/{accountId}";

  private final AccountService accountService;
  private final HouseholdService householdService;

  public RemoveCoClientEndPoint(AccountService accountService, HouseholdService householdService) {
    this.accountService = accountService;
    this.householdService = householdService;
  }

  @Transactional
  @PatchMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_account.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Removes Co-client from household.",
      description = "Removes a co-client account from the household and updates the primary contact if needed.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200", description = "OK - Co-client removed successfully."),
          @ApiResponse(responseCode = "400", description = "Bad Request",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "404", description = "Not Found",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))})
      })
  public ResponseEntity<?> removeCoClient(@PathVariable Long accountId, Principal principal) {

    log.info("User [{}] is attempting to remove co-client for account [{}]", principal.getName(), accountId);

    Account accountToRemove = accountService.getAccountById(accountId)
        .orElseThrow(() -> new IllegalArgumentException("Account with ID [" + accountId + "] not found"));

    Household household = Optional.ofNullable(accountToRemove.getHousehold())
        .orElseThrow(() -> new IllegalArgumentException("Household not found for account ID [" + accountId + "]"));

    Set<Account> activeAccounts = household.getHouseholdAccounts().stream()
        .filter(account -> !account.getId().equals(accountId) && Boolean.TRUE.equals(account.getActive()))
        .collect(Collectors.toSet());

    // If account to remove is the primary contact, we must set a new one if possible
    if (Boolean.TRUE.equals(accountToRemove.getIsPrimaryContactForHousehold())) {
      if (!activeAccounts.isEmpty()) {
        // Set the next active account as the new primary contact
        Account nextPrimaryContact = activeAccounts.iterator().next();
        nextPrimaryContact.setIsPrimaryContactForHousehold(true);
        accountService.saveAccount(nextPrimaryContact);
        log.info("Account [{}] set as the new primary contact for household [{}]",
            nextPrimaryContact.getId(), household.getId());
      } else {
        // If no active accounts remain, mark the household as inactive
        household.setActive(false);
        householdService.saveHousehold(household);
        log.info("Household [{}] set to inactive as no active accounts remain", household.getId());
      }
    }

    accountToRemove.setHousehold(null);
    accountToRemove.setIsPrimaryContactForHousehold(false);
    accountService.saveAccount(accountToRemove);
    log.info("Account [{}] removed from household [{}]", accountId, household.getId());

    return ResponseEntity.ok("Account with ID [" + accountId + "] removed successfully.");
  }
}
