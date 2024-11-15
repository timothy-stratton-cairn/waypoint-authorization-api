package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.endpoints.account.dto.AccountDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AccountListDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AccountRolesListDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AssociatedAccountDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AssociatedAccountListDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.enumeration.HouseholdRoleEnum;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Household;
import com.cairnfg.waypoint.authorization.entity.Role;
import com.cairnfg.waypoint.authorization.service.AccountService;
import com.cairnfg.waypoint.authorization.service.helper.HouseholdHelperService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Slf4j
@RestController
@Tag(name = "Account")
public class GetDependentsEndpoint {

  public static final String PATH = "/api/account/dependents";

  private final AccountService accountService;

  public GetDependentsEndpoint(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_account.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Retrieves all dependent accounts",
      description = "Retrieves all dependent accounts. Requires the `account.read` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = AccountListDto.class))}),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))}),
          @ApiResponse(responseCode = "403", description = "Forbidden",
              content = {@Content(schema = @Schema(hidden = true))})})
  public ResponseEntity<?> getDependentAccounts(Principal principal,
      @RequestParam(value = "accountId") Optional<Long[]> optionalAccountIds) {
    final ResponseEntity<?>[] response = new ResponseEntity<?>[1];
    optionalAccountIds.ifPresentOrElse(
        accountIds -> response[0] = buildFilteredAccountList(accountIds, principal.getName()),
        () -> response[0] = buildUnfilteredAccountList(principal.getName())
    );

    return response[0];
  }

private ResponseEntity<?> buildUnfilteredAccountList(String name) { //
	// TODO Auto-generated method stub
	return null;
}

private ResponseEntity<?> buildFilteredAccountList(Long[] accountIds, String name) {
	// TODO Auto-generated method stub
	return null;
}
}