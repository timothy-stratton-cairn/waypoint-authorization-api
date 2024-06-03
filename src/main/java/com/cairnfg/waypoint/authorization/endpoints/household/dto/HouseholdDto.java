package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HouseholdDto {

  private Long id;
  private String name;
  private HouseholdAccountListDto householdAccounts;
}
