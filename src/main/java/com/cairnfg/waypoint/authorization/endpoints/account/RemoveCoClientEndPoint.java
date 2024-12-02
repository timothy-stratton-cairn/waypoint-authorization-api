package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.UpdateAccountDetailsDto;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.service.AccountService;
import com.cairnfg.waypoint.authorization.service.HouseholdService;
import com.cairnfg.waypoint.authorization.service.ProtocolService;
import com.cairnfg.waypoint.authorization.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Account")
public class RemoveCoClientEndPoint {

  public static final String PATH = "/api/account/remove_coclient/{accountId}";

  private final AccountService accountService;
  private final RoleService roleService;

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
  private final ProtocolService protocolService;
  private final HouseholdService householdService;

  public RemoveCoClientEndPoint(AccountService accountService, RoleService roleService,
      ProtocolService protocolService, HouseholdService householdService) {
    this.accountService = accountService;
    this.roleService = roleService;
    this.protocolService = protocolService;
    this.householdService = householdService;
  }

  /**
   * Updates an account with the provided details and returns the result as a ResponseEntity.
   *
   * @param accountId         The ID of the account to update.
   * @param accountDetailsDto The DTO containing the details to update the account.
   * @param principal         The principal object representing the currently authenticated user.
   * @return A ResponseEntity containing the result of the account update operation.
   */
  @Transactional
  @PatchMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_account.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Removes Coclient from household. ",
      description =
          "Allows a user to remove a coclient from a household. Requires the `account.create` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              description = "OK - Account update was successful"),
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
                  schema = @Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "409", description = "Not Found",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))})})

  public ResponseEntity<?> updateHouseholdId(@PathVariable Long accountId,
      @RequestBody UpdateAccountDetailsDto accountDetailsDto,
      Principal principal) {

    log.info("User [{}] is attempting to update householdId for account [{}]", principal.getName(),
        accountId);

    // Retrieve the account
    Optional<Account> accountToUpdateOptional = this.accountService.getAccountById(accountId);
    if (accountToUpdateOptional.isEmpty()) {
      return generateFailureResponse("Account with ID [" + accountId + "] not found",
          HttpStatus.NOT_FOUND);
    }

    Account accountToUpdate = accountToUpdateOptional.get();

    // Check if isPrimaryContact is false, and if so, set householdId to null
    if (Boolean.FALSE.equals(accountToUpdate.getIsPrimaryContactForHousehold())) {
      accountToUpdate.setHousehold(null);
      log.info("householdId set to null", accountId);
    } else {
      return generateFailureResponse("Account [" + accountId + "] is the primary contact",
          HttpStatus.NOT_FOUND);
    }

    Account updatedAccount = this.accountService.saveAccount(accountToUpdate);

    log.info("Account [{}] updated successfully with ID [{}]", updatedAccount.getUsername(),
        updatedAccount.getId());
    return ResponseEntity.status(HttpStatus.OK)
        .body("Account with ID [" + updatedAccount.getId() + "] updated successfully");
  }

  private ResponseEntity<ErrorMessage> generateFailureResponse(String message, HttpStatus status) {
    log.warn(message);
    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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
