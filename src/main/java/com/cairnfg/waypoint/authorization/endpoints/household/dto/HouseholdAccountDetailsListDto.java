package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HouseholdAccountDetailsListDto {

  private List<HouseholdAccountDetailsDto> accounts;
  private Integer numberOfAccounts;

  private Integer getNumberOfAccounts() {
    return accounts != null ? accounts.size() : 0;
  }
}
