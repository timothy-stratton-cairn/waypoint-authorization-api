package com.cairnfg.waypoint.authorization.utility.password;

public class MinLengthStrategy extends PasswordValidationStrategy {

  @Override
  public boolean isValid(String password) {
    return (password.length() >= 8);
  }

  @Override
  String getRequirementType() {
    return "MIN_LENGTH";
  }

  @Override
  String getRequirementMessage() {
    return "Password must have a minimum length of 8";
  }

}
