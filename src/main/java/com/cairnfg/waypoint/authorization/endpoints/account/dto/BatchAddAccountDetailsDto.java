package com.cairnfg.waypoint.authorization.endpoints.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class BatchAddAccountDetailsDto {

  @NotBlank(message = "Username is required")
  @Size(min = 1, max = 255, message = "Username length should be between 1 and 255")
  private String username;

  @NotBlank(message = "First Name is required")
  @Size(min = 1, max = 36, message = "First Name length should be between 1 and 36")
  private String firstName;

  @NotBlank(message = "Last Name is required")
  @Size(min = 1, max = 36, message = "Last Name length should be between 1 and 36")
  private String lastName;

  @NotEmpty(message = "Inherited Roles list cannot be empty")
  private Set<String> roleNames;

  @NotBlank(message = "Enter valid email address")
  @Email(message = "Enter valid email address", regexp = ".*[a-zA-Z]+.*@.+")
  private String email;

  private String phoneNumber;
  private String address1;
  private String address2;
  private String city;
  private String state;
  private String zip;

  private String coClientUsername;
  private String parentAccountUsername;

  private String password;
}
