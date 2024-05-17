package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.UpdateAccountDetailsDto;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Household;
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
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
public class UpdateAccountDetailsEndpoint {

  public static final String PATH = "/api/account/{accountId}";

  private final AccountService accountService;
  private final RoleService roleService;

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
  private final ProtocolService protocolService;
  private final HouseholdService householdService;

  public UpdateAccountDetailsEndpoint(AccountService accountService, RoleService roleService,
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
      summary = "Allows a user to update an account, updating the account with the provided roles.",
      description =
          "Allows a user to update an account, updating the account with the provided roles. Requires the `account.create` permission.",
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
  public ResponseEntity<?> addAccount(@PathVariable Long accountId,
      @RequestBody UpdateAccountDetailsDto accountDetailsDto,
      Principal principal) {

    log.info("User [{}] is attempting to update account with ID [{}}", principal.getName(),
        accountId);

    Optional<Account> accountToUpdateOptional;
    Set<ConstraintViolation<UpdateAccountDetailsDto>> violations = validator.validate(
        accountDetailsDto);

    if ((accountToUpdateOptional = this.accountService.getAccountById(accountId)).isEmpty()) {
      return generateFailureResponse("Account with ID [" + accountId + "] not found",
          HttpStatus.NOT_FOUND);
    } else if (!violations.isEmpty()) {
      return generateFailureResponse(
          violations.stream().map(ConstraintViolation::getMessage).collect(
              Collectors.joining(", ")), HttpStatus.BAD_REQUEST);
    } else {

      Optional<Account> coClientAccountOptional;
      Account accountToUpdate = accountToUpdateOptional.get();
      Household household = accountToUpdate.getHousehold();
      accountToUpdate.setModifiedBy(principal.getName());

      if (accountDetailsDto.getRoleIds() != null) {
        try {
          accountToUpdate.setRoles(
              this.roleService.getAccountRoleMappingDetails(accountDetailsDto.getRoleIds()));
        } catch (EntityNotFoundException e) {
          return generateFailureResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
      }

      if (accountDetailsDto.getCoClientId() != null) {
        if ((coClientAccountOptional = this.accountService.getAccountById(
            accountDetailsDto.getCoClientId())).isEmpty()) {
          return generateFailureResponse("Account with ID [" + accountDetailsDto.getCoClientId()
                  + "] specified for the Co-Client not found",
              HttpStatus.NOT_FOUND);
        } else {
          accountToUpdate.setCoClient(coClientAccountOptional.get());

          coClientAccountOptional.get().setCoClient(accountToUpdate);

          if (coClientAccountOptional.get().getHousehold() != null) {
            household = coClientAccountOptional.get().getHousehold();
            accountToUpdate.setHousehold(coClientAccountOptional.get().getHousehold());
          } else {
            household = Household.builder()
                .modifiedBy(principal.getName())
                .name(
                    accountToUpdate.getLastName()
                        .equals(coClientAccountOptional.get().getLastName()) ?
                        accountToUpdate.getLastName() + " Household" :
                        accountToUpdate.getLastName() + "/" + coClientAccountOptional.get()
                            .getLastName() + " Household")
                .description(
                    accountToUpdate.getLastName()
                        .equals(coClientAccountOptional.get().getLastName()) ?
                        accountToUpdate.getLastName() + " Household" :
                        accountToUpdate.getLastName() + "/" + coClientAccountOptional.get()
                            .getLastName() + " Household")
                .householdAccounts(new HashSet<>(
                    Arrays.asList(accountToUpdate, coClientAccountOptional.get())))
                .primaryContact(accountToUpdate)
                .build();

            household = this.householdService.saveHousehold(household);

            accountToUpdate.setHousehold(household);
            coClientAccountOptional.get().setHousehold(household);
          }
        }
      }

      if (accountDetailsDto.getDependentIds() != null) {
        List<Account> dependentAccounts = this.accountService.getAccountListsByIdList(
            accountDetailsDto.getDependentIds().stream().toList());

        if (!dependentAccounts.stream()
            .map(Account::getId)
            .collect(Collectors.toSet())
            .containsAll(accountDetailsDto.getDependentIds())) {
          return generateFailureResponse(
              "Provided Account IDs for Dependents [" + accountDetailsDto.getDependentIds().stream()
                  .map(Object::toString)
                  .collect(Collectors.joining(","))
                  + "]  not found",
              HttpStatus.NOT_FOUND);
        }

        if (household == null) {
          Set<Account> householdAccounts = new HashSet<>(dependentAccounts);
          householdAccounts.add(accountToUpdate);

          household = Household.builder()
              .name(accountToUpdate.getLastName() + " Household")
              .description(accountToUpdate.getLastName() + " Household")
              .householdAccounts(householdAccounts)
              .primaryContact(accountToUpdate)
              .build();

          household = this.householdService.saveHousehold(household);
        }

        Household finalHousehold = household;
        dependentAccounts.forEach(dependentAccount -> dependentAccount.setHousehold(
            finalHousehold));

        accountToUpdate.setDependents(new HashSet<>(dependentAccounts));
      }

      if (accountDetailsDto.getFirstName() != null) {
        accountToUpdate.setFirstName(accountDetailsDto.getFirstName());
      }

      if (accountDetailsDto.getLastName() != null) {
        accountToUpdate.setLastName(accountDetailsDto.getLastName());
      }

      if (accountDetailsDto.getEmail() != null) {
        accountToUpdate.setEmail(accountDetailsDto.getEmail());
      }

      Account updatedAccount = this.accountService.saveAccount(accountToUpdate);

      if (accountDetailsDto.getCoClientId() != null) {
        try {
          protocolService.addCoClientToAllProtocolsForAccount(updatedAccount.getId(),
              updatedAccount.getCoClient().getId());
        } catch (Exception e) {
          log.warn(
              "An error occurred while assigning the CoClient to the existing Account's protocols",
              e);
          TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
          return generateFailureResponse(
              "An error occurred while assigning the CoClient to the existing Account's protocol",
              HttpStatus.UNPROCESSABLE_ENTITY);
        }
      }

      log.info("Account [{}] updated successfully with ID [{}]",
          updatedAccount.getUsername(),
          updatedAccount.getId());
      return ResponseEntity.status(HttpStatus.OK)
          .body("Account with ID [" + updatedAccount.getId() + "] updated successfully");
    }
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
