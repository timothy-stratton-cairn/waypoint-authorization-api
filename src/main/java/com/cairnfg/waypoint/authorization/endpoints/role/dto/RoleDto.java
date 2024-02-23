package com.cairnfg.waypoint.authorization.endpoints.role.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleDto {

  private Long id;
  private String name;
}
