package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchAddAccountDetailsListDto {
    private List<@Valid BatchAddAccountDetailsDto> accountBatch;

    @JsonIgnore
    private Integer numOfAccounts;

    @JsonIgnore
    public Integer getNumOfAccounts() {
        return accountBatch.size();
    }
}
