package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.dto.RequirementDto;
import com.cairnfg.waypoint.authorization.dto.Status;
import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AddAccountDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.account.mapper.AccountMapper;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Role;
import com.cairnfg.waypoint.authorization.service.AccountService;
import com.cairnfg.waypoint.authorization.service.RoleService;
import com.cairnfg.waypoint.authorization.utility.PasswordUtility;
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

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  public AddAccountEndpoint(AccountService accountService, RoleService roleService) {
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
  public ResponseEntity<?> addAccount(@RequestBody AddAccountDetailsDto accountDetailsDto,
      Principal principal) {
    log.info("User [{}] is attempting to create account with username [{}}", principal.getName(),
        accountDetailsDto.getUsername());

    Set<ConstraintViolation<AddAccountDetailsDto>> violations = validator.validate(
        accountDetailsDto);

    if (!violations.isEmpty()) {
      return generateFailureResponse(
          violations.stream().map(ConstraintViolation::getMessage).collect(
              Collectors.joining(", ")), HttpStatus.BAD_REQUEST);
    } else if (this.accountService.findByUsername(accountDetailsDto.getUsername()).isPresent()) {
      return generateFailureResponse("User with username [" +
          accountDetailsDto.getUsername() + "] already exists", HttpStatus.CONFLICT);
    } else if (PasswordUtility.validatePassword(accountDetailsDto.getPassword()).getOverallStatus()
        .equals(
            Status.FAILED)) {
      return generateFailureResponse(
          getPasswordComplexityViolations(accountDetailsDto.getPassword()),
          HttpStatus.BAD_REQUEST);
    } else {
      Set<Role> roles;
      try {
        roles = this.roleService.getAccountRoleMappingDetails(accountDetailsDto.getRoleIds());
      } catch (EntityNotFoundException e) {
        return generateFailureResponse(e.getMessage(), HttpStatus.NOT_FOUND);
      }

      Long createdAccountId = createAccount(accountDetailsDto, principal.getName(), roles);

      log.info("Account [{}] created successfully with ID [{}]", accountDetailsDto.getUsername(),
          createdAccountId);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body("Account [" + accountDetailsDto.getUsername() + "] created successfully");
    }
  }

  private Long createAccount(AddAccountDetailsDto accountDetailsDto, String modifiedBy,
      Set<Role> roles) {
    Account accountToCreate = AccountMapper.INSTANCE.accountDetailsDtoToEntity(accountDetailsDto);

    accountToCreate.setModifiedBy(modifiedBy);
    accountToCreate.setRoles(roles);

    return accountService.createAccount(accountToCreate).getId();
  }

  private String getPasswordComplexityViolations(String password) {
    return PasswordUtility.validatePassword(password).getRequirements()
        .stream()
        .filter(requirementDto -> requirementDto.getStatus().equals(Status.FAILED))
        .map(RequirementDto::getMessage).collect(Collectors.joining(", "));
  }

  private ResponseEntity<ErrorMessage> generateFailureResponse(String message, HttpStatus status) {
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
