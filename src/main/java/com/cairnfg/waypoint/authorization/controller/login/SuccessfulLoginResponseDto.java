package com.cairnfg.waypoint.authorization.controller.login;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SuccessfulLoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String idToken;
    private String expiresAt;
    private List<String> permissions;
}
