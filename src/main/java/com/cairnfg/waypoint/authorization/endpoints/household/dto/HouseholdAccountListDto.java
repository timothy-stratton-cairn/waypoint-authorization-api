package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HouseholdAccountListDto {
  private List<HouseholdAccountDto> accounts;
  private Integer numberOfAccounts;

  public Integer getNumberOfAccounts() {
    return accounts == null || accounts.isEmpty() ? 0 : accounts.size();
  }
}
