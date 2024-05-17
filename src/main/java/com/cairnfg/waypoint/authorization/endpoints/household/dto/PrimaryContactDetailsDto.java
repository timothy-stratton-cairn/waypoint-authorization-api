package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrimaryContactDetailsDto {

  private Long accountId;
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String email;
}
