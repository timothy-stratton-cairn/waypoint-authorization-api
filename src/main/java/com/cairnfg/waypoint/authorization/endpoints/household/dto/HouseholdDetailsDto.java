package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HouseholdDetailsDto {

  private Long id;
  private String name;
  private String description;
  private PrimaryContactDetailsDto primaryContact;
  private HouseholdAccountDetailsListDto householdAccounts;
}
