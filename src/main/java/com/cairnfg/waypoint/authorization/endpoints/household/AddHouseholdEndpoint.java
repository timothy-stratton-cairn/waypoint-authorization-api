package com.cairnfg.waypoint.authorization.endpoints.household;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.AddHouseholdDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.HouseholdAccountDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.HouseholdAccountDetailsListDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.HouseholdDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.PrimaryContactDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.PrimaryContactDetailsListDto;
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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Household")
public class AddHouseholdEndpoint {

  public static final String PATH = "/api/household";

  private final HouseholdService householdService;
  private final AccountService accountService;

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  public AddHouseholdEndpoint(HouseholdService householdService,
      AccountService accountService) {
    this.householdService = householdService;
    this.accountService = accountService;
  }

  @PostMapping(PATH)
  @SuppressWarnings("SimplifyStreamApiCallChains")
  @PreAuthorize("hasAnyAuthority('SCOPE_household.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Creates a household with the provided details.",
      description = "Creates a household with the provided details. Requires the `household.full` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "201",
              description = "Created - Household creation was successful",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = HouseholdDetailsDto.class))}),
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
  public ResponseEntity<?> createHousehold(@RequestBody AddHouseholdDetailsDto householdDetailsDto,
      Principal principal) {
    log.info("User [{}] is creating household with name [{}]", principal.getName(),
        householdDetailsDto.getName());

    List<Account> primaryAccounts = householdDetailsDto.getPrimaryContactAccountIds() == null ?
        List.of() : this.accountService
        .getAccountListsByIdList(householdDetailsDto.getPrimaryContactAccountIds());
    List<Account> householdAccounts = householdDetailsDto.getHouseholdAccountIds() == null ?
        List.of() : this.accountService
        .getAccountListsByIdList(householdDetailsDto.getHouseholdAccountIds());

    Set<ConstraintViolation<AddHouseholdDetailsDto>> violations = validator.validate(
        householdDetailsDto);

    if (!violations.isEmpty()) {
      return generateFailureResponse(
          violations.stream().map(ConstraintViolation::getMessage).collect(
              Collectors.joining(", ")), HttpStatus.BAD_REQUEST);
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
    } else {
      try {
        if (householdDetailsDto.getName() != null &&
            householdService.getHouseholdByName(householdDetailsDto.getName()).isPresent() &&
            !householdService.getHouseholdByName(householdDetailsDto.getName()).get()
                .getName().equals(householdDetailsDto.getName())) {
          return generateFailureResponse("Provided Household Name [" +
                  householdDetailsDto.getName() +
                  "] already exists",
              HttpStatus.CONFLICT);
        }
      } catch (NoSuchElementException e) {
        if (householdDetailsDto.getName() != null &&
            householdService.getHouseholdByNameWithoutPrimaryContactPopulation(
                householdDetailsDto.getName()).isPresent() &&
            !householdService.getHouseholdByNameWithoutPrimaryContactPopulation(
                    householdDetailsDto.getName()).get()
                .getName().equals(householdDetailsDto.getName())) {
          return generateFailureResponse("Provided Household Name [" +
                  householdDetailsDto.getName() +
                  "] already exists",
              HttpStatus.CONFLICT);
        }
      }

      Household householdToCreate = new Household();

      householdToCreate.setModifiedBy(principal.getName());
      householdToCreate.setName(householdDetailsDto.getName());
      householdToCreate.setDescription(householdDetailsDto.getDescription());
      householdToCreate.setHouseholdAccounts(new HashSet<>(householdAccounts));

      Household createdHousehold = householdService.saveHousehold(householdToCreate);

      Household finalCreatedHousehold = createdHousehold;
      householdAccounts.stream()
          .map(account -> {
            account.setModifiedBy(principal.getName());
            account.setHousehold(finalCreatedHousehold);
            account.setIsPrimaryContactForHousehold(Boolean.FALSE);
            return account;
          })
          .forEach(accountService::saveAccount);
      //###### DO NOT PUT THIS BEFORE THE ABOVE OPERATION FOR `householdAccounts` ######
      //###### IT WILL BREAK THE PRIMARY ACCOUNT FLOW ######
      primaryAccounts.stream()
          .map(account -> {
            account.setModifiedBy(principal.getName());
            account.setHousehold(finalCreatedHousehold);
            account.setIsPrimaryContactForHousehold(Boolean.TRUE);
            return account;
          })
          .forEach(accountService::saveAccount);

      createdHousehold.setPrimaryContacts(new HashSet<>(primaryAccounts));

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(HouseholdDetailsDto.builder()
              .id(createdHousehold.getId())
              .name(createdHousehold.getName())
              .description(createdHousehold.getDescription())
              .primaryContacts(PrimaryContactDetailsListDto.builder()
                  .accounts(createdHousehold.getPrimaryContacts().stream()
                      .map(primaryContact -> PrimaryContactDetailsDto.builder()
                          .accountId(primaryContact.getId())
                          .firstName(primaryContact.getFirstName())
                          .lastName(primaryContact.getLastName())
                          .phoneNumber(primaryContact.getPrimaryPhoneNumber())
                          .email(primaryContact.getEmail())
                          .build())
                      .collect(Collectors.toList()))
                  .build())
              .householdAccounts(HouseholdAccountDetailsListDto.builder()
                  .accounts(createdHousehold.getHouseholdAccounts().stream()
                      .map(account -> HouseholdAccountDetailsDto.builder()
                          .clientAccountId(account.getId())
                          .firstName(account.getFirstName())
                          .lastName(account.getLastName())
                          .build())
                      .collect(Collectors.toList()))
                  .build())
              .build()
          );
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
