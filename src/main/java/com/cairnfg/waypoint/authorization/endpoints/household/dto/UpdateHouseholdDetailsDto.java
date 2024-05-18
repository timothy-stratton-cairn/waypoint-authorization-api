package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHouseholdDetailsDto {

  private String name;
  private String description;
  private Long primaryContactAccountId;
  private List<Long> householdAccountIds;
}
