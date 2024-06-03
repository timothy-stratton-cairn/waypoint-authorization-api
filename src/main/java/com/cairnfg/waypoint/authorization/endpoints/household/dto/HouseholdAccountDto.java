package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import com.cairnfg.waypoint.authorization.endpoints.household.dto.enumeration.HouseholdRoleEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HouseholdAccountDto {

  private Long id;
  private String firstName;
  private String lastName;
  private HouseholdRoleEnum role;
}
