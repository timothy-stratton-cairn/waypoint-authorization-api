package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountDetailsDto {

  @Size(min = 1, max = 36, message = "First Name length should be between 1 and 36")
  private String firstName;

  @Size(min = 1, max = 36, message = "Last Name length should be between 1 and 36")
  private String lastName;

  private Set<Long> roleIds;

  @Email(message = "Enter valid email address", regexp = ".*[a-zA-Z]+.*@.+")
  private String email;

  private Long coClientId;
  private Set<Long> dependentIds;
  private Boolean isPrimaryHouseholdContact;
}
