package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrimaryContactDetailsListDto {

  private List<PrimaryContactDetailsDto> accounts;
  private Integer numberOfAccounts;

  private Integer getNumberOfAccounts() {
    return accounts != null ? accounts.size() : 0;
  }
}
