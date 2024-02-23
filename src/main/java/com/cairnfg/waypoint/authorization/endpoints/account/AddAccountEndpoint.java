package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.dto.RequirementDto;
import com.cairnfg.waypoint.authorization.dto.Status;
import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AccountDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.account.mapper.AccountMapper;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Role;
import com.cairnfg.waypoint.authorization.service.AccountService;
import com.cairnfg.waypoint.authorization.service.RoleService;
import com.cairnfg.waypoint.authorization.utility.PasswordUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Account")
public class AddAccountEndpoint {

  public static final String PATH = "/api/account";

  private final AccountService accountService;
  private final RoleService roleService;
  private final Validator validator;

  public AddAccountEndpoint(AccountService accountService, RoleService roleService) {
    this.accountService = accountService;
    this.roleService = roleService;

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    this.validator = factory.getValidator();
  }

  @PostMapping(PATH)
  @PreAuthorize("hasAuthority('SCOPE_account.create')")
  @Operation(
      summary = "Allows a user to create new accounts, linking the provided roles to the newly created account.",
      description =
          "Allows a user to create new accounts, linking the provided roles to the newly created account." +
              " Requires the `account.create` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "201",
              description = "Created - Account creation was successful"),
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
  public ResponseEntity<?> addAccount(@RequestBody AccountDetailsDto accountDetailsDto,
      Principal principal) throws JsonProcessingException {
    log.info("User [{}] is attempting to create account with username [{}}", principal.getName(),
        accountDetailsDto.getUsername());

    if (this.accountService.findByUsername(accountDetailsDto.getUsername()).isPresent()) {
      return generateFailureResponse("User with username [" +
          accountDetailsDto.getUsername() + "] already exists", HttpStatus.CONFLICT);
    }

    if (PasswordUtility.validatePassword(accountDetailsDto.getPassword()).getOverallStatus().equals(
        Status.FAILED)) {
      return generateFailureResponse(
          PasswordUtility.validatePassword(accountDetailsDto.getPassword()).getRequirements()
              .stream()
              .filter(requirementDto -> requirementDto.getStatus().equals(Status.FAILED))
              .map(RequirementDto::getMessage).collect(Collectors.joining(", ")),
          HttpStatus.BAD_REQUEST);
    }

    Set<Role> roles;
    try {
      roles = this.roleService.getAccountRoleMappingDetails(accountDetailsDto.getRoleIds());
    } catch (EntityNotFoundException e) {
      return generateFailureResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    Account accountToCreate = AccountMapper.INSTANCE.accountDetailsDtoToEntity(accountDetailsDto);

    accountToCreate.setModifiedBy(principal.getName());
    accountToCreate.setRoles(roles);

    Long createdAccountId = accountService.createAccount(accountToCreate);

    log.info("Account [{}] created successfully with ID [{}]", accountToCreate.getUsername(),
        createdAccountId);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body("Account [" + accountToCreate.getUsername() + "] created successfully");
  }

  public ResponseEntity<ErrorMessage> generateFailureResponse(String message, HttpStatus status) {
    log.warn(message);
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
