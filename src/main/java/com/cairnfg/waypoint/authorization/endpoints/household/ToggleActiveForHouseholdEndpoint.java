package com.cairnfg.waypoint.authorization.endpoints.household;

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
import java.util.List;
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
@Tag(name = "Household")
public class ToggleActiveForHouseholdEndpoint {

  public static final String PATH = "/api/household/toggle-active/{householdId}";

  private final AccountService accountService;
  private final HouseholdService householdService;

  public ToggleActiveForHouseholdEndpoint(HouseholdService householdService,
      AccountService accountService) {
    this.householdService = householdService;
    this.accountService = accountService;
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
  public ResponseEntity<?> toggleActiveForHousehold(@PathVariable Long householdId) {
    try {
      Optional<Household> householdOptional = householdService.getHouseholdById(householdId);

      if (householdOptional.isEmpty()) {
        log.warn("Household with ID {} not found", householdId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorMessage());
      }

      List<Account> accounts = accountService.getAccountsByHouseholdId(householdId);

      for (Account account : accounts) {
        log.info("Account: {} being set to Null", account.getId());
        account.setHousehold(null);
        accountService.saveAccount(account);
        log.info("Account: {} set to Null", account.getId());
      }

      Household household = householdOptional.get();
      boolean newState = !Boolean.TRUE.equals(household.getActive());
      household.setActive(newState);
      householdService.saveHousehold(household);

      log.info("Household with ID {} toggled to active state: {}", householdId, newState);
      return ResponseEntity.ok("Household active state toggled successfully to: " + newState);

    } catch (Exception e) {
      log.error("Error toggling active state for household with ID {}: {}", householdId,
          e.getMessage(), e);
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorMessage());
    }
  }
}
