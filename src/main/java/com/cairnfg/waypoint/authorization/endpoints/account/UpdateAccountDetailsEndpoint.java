package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.dto.RequirementDto;
import com.cairnfg.waypoint.authorization.dto.Status;
import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.UpdateAccountDetailsDto;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Account")
public class UpdateAccountDetailsEndpoint {

  public static final String PATH = "/api/account/{accountId}";

  private final AccountService accountService;
  private final RoleService roleService;

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  public UpdateAccountDetailsEndpoint(AccountService accountService, RoleService roleService) {
    this.accountService = accountService;
    this.roleService = roleService;
  }

  @PatchMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_account.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Allows a user to update an account, updating the account with the provided roles.",
      description =
          "Allows a user to update an account, updating the account with the provided roles. Requires the `account.create` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              description = "OK - Account update was successful"),
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
  public ResponseEntity<?> addAccount(@PathVariable Long accountId,
      @RequestBody UpdateAccountDetailsDto accountDetailsDto,
      Principal principal) {

    log.info("User [{}] is attempting to update account with ID [{}}", principal.getName(),
        accountId);

    Optional<Account> accountToUpdateOptional;
    Set<ConstraintViolation<UpdateAccountDetailsDto>> violations = validator.validate(
        accountDetailsDto);

    if ((accountToUpdateOptional = this.accountService.getAccountById(accountId)).isEmpty()) {
      return generateFailureResponse("Account with ID [" + accountId + "] not found",
          HttpStatus.NOT_FOUND);
    } else if (!violations.isEmpty()) {
      return generateFailureResponse(
          violations.stream().map(ConstraintViolation::getMessage).collect(
              Collectors.joining(", ")), HttpStatus.BAD_REQUEST);
    } else {

      Account accountToUpdate = accountToUpdateOptional.get();
      accountToUpdate.setModifiedBy(principal.getName());

      if (accountDetailsDto.getRoleIds() != null) {
        Set<Role> roles;
        try {
          accountToUpdate.setRoles(
              this.roleService.getAccountRoleMappingDetails(accountDetailsDto.getRoleIds()));
        } catch (EntityNotFoundException e) {
          return generateFailureResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
      }

      if (accountDetailsDto.getFirstName() != null) {
        accountToUpdate.setFirstName(accountDetailsDto.getFirstName());
      }

      if (accountDetailsDto.getLastName() != null) {
        accountToUpdate.setLastName(accountDetailsDto.getLastName());
      }

      if (accountDetailsDto.getEmail() != null) {
        accountToUpdate.setEmail(accountDetailsDto.getEmail());
      }

      Account updatedAccount = this.accountService.saveAccount(accountToUpdate);

      log.info("Account [{}] updated successfully with ID [{}]",
          updatedAccount.getUsername(),
          updatedAccount.getId());
      return ResponseEntity.status(HttpStatus.OK)
          .body("Account with ID [" + updatedAccount.getId() + "] updated successfully");
    }
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
