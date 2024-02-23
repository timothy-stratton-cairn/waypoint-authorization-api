package com.cairnfg.waypoint.authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequirementDto {

  private String requirement;
  private Enum<Status> status;
  private String message;

  public static RequirementDto getRequirement(String requirement, Enum<Status> status,
      String message) {
    return RequirementDto.builder().requirement(requirement).status(status).message(message)
        .build();
  }
}
