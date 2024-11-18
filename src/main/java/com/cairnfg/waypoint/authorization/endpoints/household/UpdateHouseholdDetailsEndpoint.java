package com.cairnfg.waypoint.authorization.endpoints.household;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.UpdateHouseholdDetailsDto;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
@Tag(name = "Household")
public class UpdateHouseholdDetailsEndpoint {

  public static final String PATH = "/api/household/{householdId}";

  private final HouseholdService householdService;
  private final AccountService accountService;

  public UpdateHouseholdDetailsEndpoint(HouseholdService householdService,
      AccountService accountService) {
    this.householdService = householdService;
    this.accountService = accountService;
  }

  @Transactional
  @SuppressWarnings("SimplifyStreamApiCallChains")
  @PatchMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_household.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Updates a household with the provided details.",
      description = "Updates a household with the provided details. Requires the `household.full` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              description = "OK - Household update was successful"),
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

  public ResponseEntity<?> updateHouseholdById(@PathVariable Long householdId,
      @RequestBody UpdateHouseholdDetailsDto householdDetailsDto, Principal principal) {
    log.info("User [{}] is updating household detail with ID [{}]", principal.getName(),
        householdId);

    Optional<Household> householdToUpdateOptional = this.householdService.getHouseholdById(
        householdId);
    List<Account> primaryAccounts = householdDetailsDto.getPrimaryContactAccountIds() == null ?
        List.of() : this.accountService
        .getAccountListsByIdList(householdDetailsDto.getPrimaryContactAccountIds());
    List<Account> householdAccounts = householdDetailsDto.getHouseholdAccountIds() == null ?
        List.of() : this.accountService
        .getAccountListsByIdList(householdDetailsDto.getHouseholdAccountIds());
    if (householdToUpdateOptional.isEmpty()) {
      return generateFailureResponse("Household with ID [" + householdId + "] not found",
          HttpStatus.NOT_FOUND);
    } else if (householdDetailsDto.getPrimaryContactAccountIds() != null &&
        !householdDetailsDto.getPrimaryContactAccountIds().isEmpty()
        && !new HashSet<>(primaryAccounts.stream()
        .map(Account::getId)
        .toList())
        .containsAll(householdDetailsDto.getPrimaryContactAccountIds())) {
      return generateFailureResponse("Provided Account with IDs [" +
              householdDetailsDto.getPrimaryContactAccountIds().stream()
                  .map(Objects::toString)
                  .collect(Collectors.joining(",")) +
              "] provided for the primary accounts of the household not found",
          HttpStatus.NOT_FOUND);
    } else if (householdDetailsDto.getHouseholdAccountIds() != null &&
        !householdDetailsDto.getHouseholdAccountIds().isEmpty()
        && !new HashSet<>(householdAccounts.stream()
        .map(Account::getId)
        .toList())
        .containsAll(householdDetailsDto.getHouseholdAccountIds())) {
      return generateFailureResponse("Provided Account with IDs [" +
              householdDetailsDto.getHouseholdAccountIds().stream()
                  .map(Objects::toString)
                  .collect(Collectors.joining(",")) +
              "]  provided for the household accounts not found",
          HttpStatus.NOT_FOUND);
    } else if (householdDetailsDto.getName() != null &&
        householdService.getHouseholdByName(householdDetailsDto.getName()).isPresent() &&
        !householdService.getHouseholdByName(householdDetailsDto.getName()).get()
            .getName().equals(householdDetailsDto.getName())) {
      return generateFailureResponse("Provided Household Name [" +
              householdDetailsDto.getName() +
              "] already exists",
          HttpStatus.CONFLICT);
    } else {
      Household householdToUpdate = householdToUpdateOptional.get();

      if (householdDetailsDto.getName() != null) {
        householdToUpdate.setName(householdDetailsDto.getName());
      }

      if (householdDetailsDto.getDescription() != null) {
        householdToUpdate.setDescription(householdDetailsDto.getDescription());
      }

      if (householdDetailsDto.getHouseholdAccountIds() != null &&
          !householdDetailsDto.getHouseholdAccountIds().isEmpty()) {
        householdToUpdate.getHouseholdAccounts().forEach(account -> {
          account.setModifiedBy(principal.getName());
          account.setHousehold(null);
          account.setIsPrimaryContactForHousehold(null);
        });

        accountService.saveAllAccounts(householdToUpdate.getHouseholdAccounts());

        householdAccounts.forEach(account -> {
          account.setModifiedBy(principal.getName());
          account.setHousehold(householdToUpdate);
          accountService.saveAccount(account);
        });
        householdToUpdate.setHouseholdAccounts(new HashSet<>(householdAccounts));
      }

      if (householdDetailsDto.getPrimaryContactAccountIds() != null) {
        List<Account> accountsNoLongerPrimaryContactOfHousehold = householdToUpdate.getPrimaryContacts()
            .stream()
            .map(account -> {
              account.setModifiedBy(principal.getName());
              account.setIsPrimaryContactForHousehold(Boolean.FALSE);
              return account;
            })
            .collect(Collectors.toList());
        accountsNoLongerPrimaryContactOfHousehold.forEach(accountService::saveAccount);

        List<Account> newPrimaryAccounts = primaryAccounts.stream()
            .map(account -> {
              account.setModifiedBy(principal.getName());
              account.setIsPrimaryContactForHousehold(Boolean.TRUE);
              return account;
            })
            .collect(Collectors.toList());
        newPrimaryAccounts.forEach(accountService::saveAccount);
      }

      Household updatedHousehold = householdService.saveHousehold(householdToUpdate);

      return ResponseEntity.status(HttpStatus.OK)
          .body("Household with ID [" + updatedHousehold.getId() + "] updated successfully");
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
