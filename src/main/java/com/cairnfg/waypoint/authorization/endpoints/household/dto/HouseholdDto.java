package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HouseholdDto {

  private Long id;
  private String name;
  private Integer numOfAccountsInHousehold;
  private Long primaryContactAccountId;
}
