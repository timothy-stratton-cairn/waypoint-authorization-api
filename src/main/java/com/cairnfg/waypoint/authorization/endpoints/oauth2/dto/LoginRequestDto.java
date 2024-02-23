package com.cairnfg.waypoint.authorization.endpoints.oauth2.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequestDto {

  private String username;
  private String password;
}
