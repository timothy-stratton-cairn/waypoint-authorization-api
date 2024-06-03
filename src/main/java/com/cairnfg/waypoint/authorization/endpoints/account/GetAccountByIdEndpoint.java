package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AccountDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.account.mapper.AccountMapper;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Role;
import com.cairnfg.waypoint.authorization.service.AccountService;
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
@Tag(name = "Account")
public class GetAccountByIdEndpoint {

  public static final String PATH = "/api/account/{accountId}";

  private final AccountService accountService;

  public GetAccountByIdEndpoint(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_account.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Retrieves am account by it's ID.",
      description = "Retrieves an account by it's ID. Requires the `account.read` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = AccountDetailsDto.class))}),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))}),
          @ApiResponse(responseCode = "403", description = "Forbidden",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))}),
          @ApiResponse(responseCode = "404", description = "Not Found",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))})})
  public ResponseEntity<?> getAccountById(@PathVariable Long accountId,
      Principal principal) {
    log.info("User [{}] is Retrieving Account with ID [{}]", principal.getName(),
        accountId);

    final ResponseEntity<?>[] response = new ResponseEntity<?>[1];
    this.accountService.getAccountById(accountId)
        .ifPresentOrElse(
            returnedAccount -> response[0] = generateSuccessResponse(
                returnedAccount),
            () -> response[0] = generateFailureResponse(accountId)
        );

    return response[0];
  }

  public ResponseEntity<AccountDetailsDto> generateSuccessResponse(
      Account returnedAccount) {
    return ResponseEntity.ok(
        AccountDetailsDto.builder()
            .id(returnedAccount.getId())
            .username(returnedAccount.getUsername())
            .firstName(returnedAccount.getFirstName())
            .lastName(returnedAccount.getLastName())
            .email(returnedAccount.getEmail())
            .roles(returnedAccount.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()))
            .coClient(AccountMapper.INSTANCE.accountToLinkedAccountDetailsDto(
                returnedAccount.getCoClient()))
            .dependents(returnedAccount.getDependents().stream()
                .map(AccountMapper.INSTANCE::accountToLinkedAccountDetailsDto)
                .collect(Collectors.toSet()))
            .householdId(returnedAccount.getHousehold() != null ?
                returnedAccount.getHousehold().getId() : null)
            .build()
    );
  }

  public ResponseEntity<ErrorMessage> generateFailureResponse(Long protocolTemplateId) {
    log.info("User Account with ID [{}] not found", protocolTemplateId);
    return new ResponseEntity<>(
        ErrorMessage.builder()
            .path(PATH)
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("User Account with ID [" + protocolTemplateId + "] not found")
            .build(),
        HttpStatus.NOT_FOUND
    );
  }
}
