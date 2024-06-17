package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompletePasswordResetDto {

  private String username;
  private String passwordResetToken;
  private String newPassword;
}
