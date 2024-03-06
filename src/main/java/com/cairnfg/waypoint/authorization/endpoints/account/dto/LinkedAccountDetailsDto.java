package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkedAccountDetailsDto {

  private Long id;
  private String firstName;
  private String lastName;
  private String username;
}
