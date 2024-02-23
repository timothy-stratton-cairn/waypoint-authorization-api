package com.cairnfg.waypoint.authorization.utility.password;

import org.apache.commons.lang3.StringUtils;

public class IsEmptyStrategy extends PasswordValidationStrategy {

  @Override
  public boolean isValid(String password) {
    return StringUtils.isNotEmpty(password);
  }

  @Override
  String getRequirementType() {
    return "MIN_LENGTH";
  }

  @Override
  String getRequirementMessage() {
    return "Password cannot be empty";
  }

}
