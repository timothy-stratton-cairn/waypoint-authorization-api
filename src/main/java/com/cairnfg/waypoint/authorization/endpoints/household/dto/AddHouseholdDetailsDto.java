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
  @NotNull
  @PositiveOrZero
  private Long primaryContactAccountId;
  @NotNull
  private List<@PositiveOrZero Long> householdAccountIds;
}
