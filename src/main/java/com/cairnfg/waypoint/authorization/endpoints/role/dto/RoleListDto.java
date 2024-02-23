package com.cairnfg.waypoint.authorization.endpoints.role.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleListDto {

  private List<RoleDto> roles;
  private Integer numOfRoles;

  public Integer getNumOfRoles() {
    return roles.size();
  }
}
