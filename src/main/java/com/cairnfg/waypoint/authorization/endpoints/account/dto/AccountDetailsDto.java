package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDetailsDto {

  private String username;
  private String firstName;
  private String lastName;
  private Set<String> roles;
  private String email;
  private LinkedAccountDetailsDto coClient;
  private Set<LinkedAccountDetailsDto> dependents;
}
