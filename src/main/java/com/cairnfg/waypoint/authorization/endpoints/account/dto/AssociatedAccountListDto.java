package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssociatedAccountListDto {

  private List<AssociatedAccountDto> accounts;
  private Integer numberOfAssociatedAccounts;

  public Integer getNumberOfAssociatedAccounts() {
    return accounts.isEmpty() ? 0 : accounts.size();
  }
}
