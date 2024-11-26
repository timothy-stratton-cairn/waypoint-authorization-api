package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.endpoints.account.dto.AccountDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AccountListDto;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.AccountRelationship;
import com.cairnfg.waypoint.authorization.service.AccountRelationshipService;
import com.cairnfg.waypoint.authorization.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Account")
public class GetUnassignedAccountsEndpoint {

  public static final String PATH = "/account/unassigned";

  private final AccountRelationshipService relationshipService;
  private final AccountService accountService;

  public GetUnassignedAccountsEndpoint(AccountService accountService,
      AccountRelationshipService relationshipService) {
    this.accountService = accountService;
    this.relationshipService = relationshipService;
  }

  @GetMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_account.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Retrieves all Unassigned Accounts",
      description = "Retrieves all accounts not part of a household and not a dependent",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = AccountListDto.class))}),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))}),
          @ApiResponse(responseCode = "403", description = "Forbidden",
              content = {@Content(schema = @Schema(hidden = true))})
      })
  public ResponseEntity<AccountListDto> getUnassignedAccounts(Principal principal) {
    log.info("User [{}] is retrieving unassigned accounts", principal.getName());

    List<Account> allAccounts = accountService.getAllAccounts();

    Set<Long> dependentIds = relationshipService.findAll().stream()
        .map(AccountRelationship::getDependent)
        .filter(Objects::nonNull)
        .map(Account::getId)
        .collect(Collectors.toSet());

    List<AccountDto> unassignedAccounts = allAccounts.stream()
        .filter(
            account -> account.getHousehold() == null && !dependentIds.contains(account.getId()))
        .map(account -> AccountDto.builder()
            .id(account.getId())
            .firstName(account.getFirstName())
            .lastName(account.getLastName())
            .email(account.getEmail())
            .build())
        .collect(Collectors.toList());

    return ResponseEntity.ok(AccountListDto.builder().accounts(unassignedAccounts).build());
  }
}

