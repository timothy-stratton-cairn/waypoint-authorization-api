package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HouseholdAccountDetailsDto {

  private Long clientAccountId;
  private String firstName;
  private String lastName;
}
