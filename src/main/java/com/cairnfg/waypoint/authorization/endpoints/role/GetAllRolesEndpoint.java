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
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
  @PreAuthorize("hasAnyAuthority('SCOPE_role.full', 'SCOPE_admin.full')")
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
  public ResponseEntity<?> getAllRoles(Principal principal,
      @RequestParam(value = "roleId") Optional<Long[]> optionalRoleIds) {
    final ResponseEntity<?>[] response = new ResponseEntity<?>[1];
    optionalRoleIds.ifPresentOrElse(
        accountIds -> response[0] = buildFilteredAccountList(accountIds, principal.getName()),
        () -> response[0] = buildUnfilteredAccountList(principal.getName())
    );

    return response[0];
  }

  private ResponseEntity<RoleListDto> buildFilteredAccountList(Long[] roleIds,
      String modifiedBy) {
    log.info("User [{}] is Retrieving Roles with ID List [{}]", modifiedBy,
        roleIds);
    return ResponseEntity.ok(
        RoleListDto.builder()
            .roles(
                this.roleService.getAllRoles(List.of(roleIds)).stream()
                    .map(RoleMapper.INSTANCE::toDto)
                    .toList())
            .build()
    );
  }

  private ResponseEntity<RoleListDto> buildUnfilteredAccountList(String modifiedBy) {
    log.info("User [{}] is retrieving all Roles", modifiedBy);
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
