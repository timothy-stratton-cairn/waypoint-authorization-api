package com.cairnfg.waypoint.authorization.endpoints.account;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Household")
public class RemoveCoClientEndPoint {

    public static final String PATH = "/api/account/remove_coclient/{accountId}";

    private final AccountService accountService;

    public RemoveCoClientEndPoint(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Removes a co-client from the household.
     *
     * @param accountId The ID of the account to update.
     * @param principal The principal object representing the currently authenticated user.
     * @return A ResponseEntity containing the result of the operation.
     */
    @Transactional
    @PatchMapping(PATH)
    @PreAuthorize("hasAnyAuthority('SCOPE_account.full', 'SCOPE_admin.full')")
    @Operation(
            summary = "Removes Co-client from household",
            description = "Allows a user to remove a co-client from a household. Requires the `account.create` permission.",
            security = @SecurityRequirement(name = "oAuth2JwtBearer"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Account update was successful"),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class))}),
                    @ApiResponse(responseCode = "409", description = "Conflict",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class))})
            }
    )
    public ResponseEntity<?> removeCoClient(@PathVariable Long accountId, Principal principal) {
        log.info("User [{}] is attempting to remove co-client for account [{}]", principal.getName(), accountId);
        try {
            accountService.removeCoClientFromHousehold(accountId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Account with ID [" + accountId + "] successfully removed from household");
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
