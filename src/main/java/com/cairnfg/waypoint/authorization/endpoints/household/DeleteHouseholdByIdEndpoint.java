package com.cairnfg.waypoint.authorization.endpoints.household;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.service.AccountRelationshipService;
import com.cairnfg.waypoint.authorization.service.AccountService;
import com.cairnfg.waypoint.authorization.service.HouseholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import java.util.List;
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
@Tag(name = "Household")
public class DeleteHouseholdByIdEndpoint {
  public static final String PATH ="/api/household/{householdId}";
  private final HouseholdService householdService;
  private final AccountService accountService;
  private final AccountRelationshipService accountRelationshipService;

  public DeleteHouseholdByIdEndpoint(HouseholdService householdService,
      AccountService accountService, AccountRelationshipService accountRelationshipService) {
    this.householdService = householdService;
    this.accountService = accountService;
    this.accountRelationshipService = accountRelationshipService;
  }

  @Transactional
  @DeleteMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_household.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Deletes a household by its ID.",
      description = "Deletes a household by its ID. Requires `household.full` or `admin.full` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "204", description = "No Content - Household deleted successfully"),
          @ApiResponse(responseCode = "404", description = "Not Found - Household does not exist",
              content = {@io.swagger.v3.oas.annotations.media.Content(
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
      }
  )
  public ResponseEntity<?> deleteHousehold(@PathVariable Long householdId) {
    try {
      log.info("Fetching accounts associated with household ID: {}", householdId);

      List<Account> accounts = accountService.getAccountsByHouseholdId(householdId);

      accounts.forEach(account -> {
        account.setHousehold(null);
        accountService.saveAccount(account); // Save the updated account
      });

      log.info("All related accounts have been disassociated from household ID: {}", householdId);

      householdService.deleteById(householdId);

      log.info("Household with ID {} deleted successfully.", householdId);
      return ResponseEntity.noContent().build();
    } catch (NoSuchElementException e) {
      log.error("Household with ID {} not found.", householdId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ErrorMessage());
    } catch (Exception e) {
      log.error("Error occurred while deleting household with ID {}: {}", householdId,
          e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorMessage());
    }

  }
}
