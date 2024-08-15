package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import com.cairnfg.waypoint.authorization.endpoints.household.dto.enumeration.HouseholdRoleEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssociatedAccountDto {

  private Long id;
  private String username;
  private String firstName;
  private String lastName;
  private HouseholdRoleEnum role;
}
