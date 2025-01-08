package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.service.AccountRelationshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Account")
public class RemoveDependentFromUserEndpoint {

  public static final String PATH = "/api/account/dependents/remove";

  private final AccountRelationshipService accountRelationshipService;

  public RemoveDependentFromUserEndpoint(AccountRelationshipService accountRelationshipService) {
    this.accountRelationshipService = accountRelationshipService;
  }

  @GetMapping(PATH)
  @PreAuthorize("hasAnyAuthority('SCOPE_account.full', 'SCOPE_admin.full')")
  @Operation(
      summary = "Removes a dependent account from a user",
      description = "Removes the dependent account associated with a specific main account. Requires the `account.modify` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200", description = "Dependent removed successfully",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = String.class))}),
          @ApiResponse(responseCode = "400", description = "Invalid request",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = String.class))}),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))}),
          @ApiResponse(responseCode = "403", description = "Forbidden",
              content = {@Content(schema = @Schema(hidden = true))})
      }
  )
  public ResponseEntity<String> removeDependentFromUser(
      Principal principal,
      @RequestParam("mainAccountId") Long mainAccountId,
      @RequestParam("dependentId") Long dependentId) {

    log.info("User [{}] is attempting to remove dependent [{}] from main account [{}]",
        principal.getName(), dependentId, mainAccountId);

    try {
      accountRelationshipService.removeDependentFromUser(mainAccountId, dependentId);
      log.info("Dependent [{}] removed successfully from main account [{}] by user [{}]",
          dependentId, mainAccountId, principal.getName());
      return ResponseEntity.ok("Dependent removed successfully.");
    } catch (IllegalArgumentException e) {
      log.error("Failed to remove dependent [{}] from main account [{}]: {}",
          dependentId, mainAccountId, e.getMessage());
      return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
    } catch (Exception e) {
      log.error("An unexpected error occurred while removing dependent [{}] from main account [{}]: {}",
          dependentId, mainAccountId, e.getMessage());
      return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
    }
  }
}
