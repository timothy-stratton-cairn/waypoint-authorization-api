package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HouseholdListDto {

  private List<HouseholdDto> households;
  private Integer numOfHouseholds;

  public Integer getNumOfHouseholds() {
    return households.size();
  }

}
