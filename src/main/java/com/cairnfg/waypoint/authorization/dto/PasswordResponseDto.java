package com.cairnfg.waypoint.authorization.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResponseDto {

  private Enum<Status> overallStatus;
  private List<RequirementDto> requirements;
}
