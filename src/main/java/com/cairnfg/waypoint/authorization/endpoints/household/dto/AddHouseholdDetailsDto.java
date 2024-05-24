package com.cairnfg.waypoint.authorization.endpoints.household.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddHouseholdDetailsDto {

  @NotBlank(message = "Household Name is required")
  private String name;
  private String description;
  @NotNull(message = "A Primary Account ID must be provided")
  private List<@PositiveOrZero(message = "Account IDs cannot be negative") Long> primaryContactAccountIds;
  @NotNull(message = "At least one Household Account ID (the primary account) must be provided")
  private List<@PositiveOrZero(message = "Account IDs cannot be negative") Long> householdAccountIds;
}
