package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchAddAccountsResponseDto {

    private List<AddAccountResponseDto> accountCreationResponses;
    private String overallStatus;
    private Integer numberOfAccounts;

    public Integer getNumberOfAccounts() {
        return accountCreationResponses.size();
    }

    public String getOverallStatus() {
        return accountCreationResponses.stream()
            .anyMatch(AddAccountResponseDto::getError) ? "FAILED" : "SUCCESS";
    }
}

