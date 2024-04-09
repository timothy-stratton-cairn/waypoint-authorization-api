package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDto {

  private String oldPassword;
  private String newPassword;
}
