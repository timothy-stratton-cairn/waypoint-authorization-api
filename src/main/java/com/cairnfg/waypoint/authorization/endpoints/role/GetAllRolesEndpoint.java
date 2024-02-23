package com.cairnfg.waypoint.authorization.endpoints.role;

import com.cairnfg.waypoint.authorization.endpoints.role.dto.RoleListDto;
import com.cairnfg.waypoint.authorization.endpoints.role.mapper.RoleMapper;
import com.cairnfg.waypoint.authorization.service.RoleService;
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
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Role")
public class GetAllRolesEndpoint {

  public static final String PATH = "/api/role";

  private final RoleService roleService;

  public GetAllRolesEndpoint(RoleService roleService) {
    this.roleService = roleService;
  }

  @GetMapping(PATH)
  @PreAuthorize("hasAuthority('SCOPE_role.read')")
  @Operation(
      summary = "Retrieves all roles.",
      description = "Retrieves all roles. Requires the `role.read` permission.",
      security = @SecurityRequirement(name = "oAuth2JwtBearer"),
      responses = {
          @ApiResponse(responseCode = "200",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = RoleListDto.class))}),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))}),
          @ApiResponse(responseCode = "403", description = "Forbidden",
              content = {@Content(schema = @Schema(hidden = true))})})
  public ResponseEntity<RoleListDto> getAllRoles(Principal principal) {
    log.info("User [{}] is retrieving all Roles", principal.getName());
    return ResponseEntity.ok(
        RoleListDto.builder()
            .roles(
                this.roleService.getAllRoles().stream()
                    .map(RoleMapper.INSTANCE::toDto)
                    .toList())
            .build()
    );
  }
}
