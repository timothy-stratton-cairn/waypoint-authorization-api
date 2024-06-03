package com.cairnfg.waypoint.authorization.endpoints.household;

import com.cairnfg.waypoint.authorization.endpoints.household.dto.HouseholdAccountDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.HouseholdAccountListDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.HouseholdDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.HouseholdListDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.enumeration.HouseholdRoleEnum;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Household;
import com.cairnfg.waypoint.authorization.service.HouseholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Household")
public class GetAllHouseholdsEndpoint {

  public static final String PATH = "/api/household";

  private final HouseholdService householdService;

  public GetAllHouseholdsEndpoint(HouseholdService householdService) {
    this.householdService = householdService;
  }

  @GetMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_household.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Retrieves all households.",
      description = "Retrieves all households. Requires the `household.read` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = HouseholdListDto.class))}),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))}),
          @ApiResponse(responseCode = "403", description = "Forbidden",
              content = {@Content(schema = @Schema(hidden = true))})})
  public ResponseEntity<?> getAllHouseholds(Principal principal,
      @RequestParam(value = "householdId") Optional<Long[]> optionalHouseholdIds) {
    final ResponseEntity<?>[] response = new ResponseEntity<?>[1];
    optionalHouseholdIds.ifPresentOrElse(
        accountIds -> response[0] = buildFilteredHouseholdList(accountIds, principal.getName()),
        () -> response[0] = buildUnfilteredHouseholdList(principal.getName())
    );

    return response[0];
  }

  private ResponseEntity<HouseholdListDto> buildFilteredHouseholdList(Long[] householdIds,
      String modifiedBy) {
    log.info("User [{}] is Retrieving Households with ID List [{}]", modifiedBy,
        householdIds);
    return ResponseEntity.ok(
        HouseholdListDto.builder()
            .households(
                this.householdService.getHouseholdListsByIdList(List.of(householdIds)).stream()
                    .map(household -> HouseholdDto.builder()
                        .id(household.getId())
                        .name(household.getName())
                        .householdAccounts(HouseholdAccountListDto.builder()
                            .accounts(household.getHouseholdAccounts().stream()
                                .map(account -> HouseholdAccountDto.builder()
                                    .id(account.getId())
                                    .firstName(account.getFirstName())
                                    .lastName(account.getLastName())
                                    .role(getHouseholdRole(household, account))
                                    .build())
                                .collect(Collectors.toList()))
                            .build())
                        .build())
                    .toList())
            .build()
    );
  }

  private ResponseEntity<HouseholdListDto> buildUnfilteredHouseholdList(String modifiedBy) {
    log.info("User [{}] is retrieving all Households", modifiedBy);
    return ResponseEntity.ok(
        HouseholdListDto.builder()
            .households(
                this.householdService.getAllHouseholds().stream()
                    .map(household -> HouseholdDto.builder()
                        .id(household.getId())
                        .name(household.getName())
                        .householdAccounts(HouseholdAccountListDto.builder()
                            .accounts(household.getHouseholdAccounts().stream()
                                .map(account -> HouseholdAccountDto.builder()
                                    .id(account.getId())
                                    .firstName(account.getFirstName())
                                    .lastName(account.getLastName())
                                    .role(getHouseholdRole(household, account))
                                    .build())
                                .collect(Collectors.toList()))
                            .build())
                        .build())
                    .toList())
            .build()
    );
  }

  private HouseholdRoleEnum getHouseholdRole(Household household, Account account) {
    if (household.getPrimaryContacts().contains(account)) {
      return HouseholdRoleEnum.PRIMARY_CONTACT;
    } else if (account.getCoClient() != null) {
      return HouseholdRoleEnum.CO_CLIENT;
    } else {
      return HouseholdRoleEnum.DEPENDENT;
    }
  }
}
