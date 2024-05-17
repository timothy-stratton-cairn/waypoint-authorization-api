package com.cairnfg.waypoint.authorization.endpoints.household;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.HouseholdAccountDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.HouseholdAccountDetailsListDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.HouseholdDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.PrimaryContactDetailsDto;
import com.cairnfg.waypoint.authorization.entity.Household;
import com.cairnfg.waypoint.authorization.service.HouseholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Household")
public class GetHouseholdByIdEndpoint {

  public static final String PATH = "/api/household/{householdId}";

  private final HouseholdService householdService;

  public GetHouseholdByIdEndpoint(HouseholdService householdService) {
    this.householdService = householdService;
  }

  @GetMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_household.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Retrieves am household by it's ID.",
      description = "Retrieves an household by it's ID. Requires the `household.read` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = HouseholdDetailsDto.class))}),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))}),
          @ApiResponse(responseCode = "403", description = "Forbidden",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "404", description = "Not Found",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))})})
  public ResponseEntity<?> getHouseholdById(@PathVariable Long householdId,
      Principal principal) {
    log.info("User [{}] is Retrieving Household with ID [{}]", principal.getName(),
        householdId);

    final ResponseEntity<?>[] response = new ResponseEntity<?>[1];
    this.householdService.getHouseholdById(householdId)
        .ifPresentOrElse(
            returnedHousehold -> response[0] = generateSuccessResponse(
                returnedHousehold),
            () -> response[0] = generateFailureResponse(householdId)
        );

    return response[0];
  }

  public ResponseEntity<HouseholdDetailsDto> generateSuccessResponse(
      Household returnedHousehold) {
    return ResponseEntity.ok(
        HouseholdDetailsDto.builder()
            .id(returnedHousehold.getId())
            .name(returnedHousehold.getName())
            .description(returnedHousehold.getDescription())
            .primaryContact(PrimaryContactDetailsDto.builder()
                .accountId(returnedHousehold.getPrimaryContact().getId())
                .firstName(returnedHousehold.getPrimaryContact().getFirstName())
                .lastName(returnedHousehold.getPrimaryContact().getLastName())
                .phoneNumber(returnedHousehold.getPrimaryContact().getPrimaryPhoneNumber())
                .email(returnedHousehold.getPrimaryContact().getEmail())
                .build())
            .householdAccounts(HouseholdAccountDetailsListDto.builder()
                .accounts(returnedHousehold.getHouseholdAccounts().stream()
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

  public ResponseEntity<ErrorMessage> generateFailureResponse(Long protocolTemplateId) {
    log.info("Protocol Template with ID [{}] not found", protocolTemplateId);
    return new ResponseEntity<>(
        ErrorMessage.builder()
            .path(PATH)
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Protocol Template with ID [" + protocolTemplateId + "] not found")
            .build(),
        HttpStatus.NOT_FOUND
    );
  }
}