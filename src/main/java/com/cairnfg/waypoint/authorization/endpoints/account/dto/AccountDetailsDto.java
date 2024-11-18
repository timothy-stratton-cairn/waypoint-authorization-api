package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDetailsDto {

  private Long id;
  private String username;
  private String firstName;
  private String lastName;
  private Set<String> roles;
  private String email;
  private String phone;
  private String address;
  private String city;
  private String state;
  private LinkedAccountDetailsDto coClient;
  private Set<LinkedAccountDetailsDto> dependents;
  private Long householdId;
}
