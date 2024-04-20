package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AddAccountDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AddAccountResponseDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.BatchAddAccountDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.BatchAddAccountDetailsListDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.BatchAddAccountsResponseDto;
import com.cairnfg.waypoint.authorization.endpoints.account.mapper.AccountMapper;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Role;
import com.cairnfg.waypoint.authorization.service.AccountService;
import com.cairnfg.waypoint.authorization.service.RoleService;
import com.github.javafaker.Faker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Tag(name = "Account")
public class BatchAddAccountsEndpoint {

  public static final String PATH = "/api/account/batch";

  private final AccountService accountService;
  private final RoleService roleService;

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  public BatchAddAccountsEndpoint(AccountService accountService, RoleService roleService) {
    this.accountService = accountService;
    this.roleService = roleService;
  }

  @PostMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_account.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Allows a user to create new accounts, linking the provided roles to the newly created account.",
      description =
          "Allows a user to create new accounts, linking the provided roles to the newly created account."
              +
              " Requires the `account.create` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              description = "Created - Account creation was successful"),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))}),
          @ApiResponse(responseCode = "403", description = "Forbidden",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = ErrorMessage.class))})})
  public ResponseEntity<BatchAddAccountsResponseDto> addAccount(@RequestBody BatchAddAccountDetailsListDto batchAddAccountDetailsListDto,
      Principal principal) {
    log.info("User [{}] is attempting to create [{}] account with usernames [{}}", principal.getName(),
            batchAddAccountDetailsListDto.getNumOfAccounts(), batchAddAccountDetailsListDto.getAccountBatch().stream()
                    .map(BatchAddAccountDetailsDto::getUsername).collect(Collectors.joining(", ")));

    return ResponseEntity.ok(BatchAddAccountsResponseDto.builder()
            .accountCreationResponses(batchAddAccountDetailsListDto.getAccountBatch().stream()
                .map(accountToBeCreated -> this.setupAndCreateAccount(accountToBeCreated, principal.getName()))
                .toList())
            .build());
  }

  private AddAccountResponseDto setupAndCreateAccount(BatchAddAccountDetailsDto accountDetailsDto, String modifiedBy) {
    Set<ConstraintViolation<BatchAddAccountDetailsDto>> violations = validator.validate(
        accountDetailsDto);

    if (!violations.isEmpty()) {
      return AddAccountResponseDto.builder()
          .username(accountDetailsDto.getUsername())
          .error(Boolean.TRUE)
          .message(violations.stream().map(ConstraintViolation::getMessage).collect(
              Collectors.joining(", ")))
          .build();
    } else if (this.accountService.findByUsername(accountDetailsDto.getUsername()).isPresent()) {
      return AddAccountResponseDto.builder()
          .username(accountDetailsDto.getUsername())
          .error(Boolean.TRUE)
          .message("User with username [" +
              accountDetailsDto.getUsername() + "] already exists")
          .build();
    }

    Set<Role> roles;
    try {
      roles = this.roleService.getRolesByName(accountDetailsDto.getRoleNames());
    } catch (EntityNotFoundException e) {
      return AddAccountResponseDto.builder()
          .username(accountDetailsDto.getUsername())
          .error(Boolean.TRUE)
          .message(e.getMessage())
          .build();
    }

    Account createdAccount = createAccount(
        accountDetailsDto,
        Faker.instance().internet()
          .password(8,36, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE),
        modifiedBy,
        roles);

    return AddAccountResponseDto.builder()
        .accountId(createdAccount.getId())
        .username(createdAccount.getUsername())
        .error(Boolean.FALSE)
        .message("201 - Account created successfully")
        .build();
  }

  private Account createAccount(BatchAddAccountDetailsDto accountDetailsDto, String password, String modifiedBy,
      Set<Role> roles) {
    Account accountToCreate = AccountMapper.INSTANCE.accountDetailsDtoToEntity(accountDetailsDto);

    accountToCreate.setModifiedBy(modifiedBy);
    accountToCreate.setPassword(password);
    accountToCreate.setRoles(roles);

    return accountService.createAccount(accountToCreate);
  }
}
