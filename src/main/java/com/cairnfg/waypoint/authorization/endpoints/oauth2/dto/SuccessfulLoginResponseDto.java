package com.cairnfg.waypoint.authorization.endpoints.oauth2.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessfulLoginResponseDto {

  private String accessToken;
  private String refreshToken;
  private String idToken;
  private Long expiresIn;
  private List<String> permissions;
}
