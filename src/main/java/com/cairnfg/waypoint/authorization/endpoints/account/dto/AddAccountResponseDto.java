package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class AddAccountResponseDto {

    private Long accountId;
    private String username;
    private Boolean error;
    private String message;
}
