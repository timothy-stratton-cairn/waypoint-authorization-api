package com.cairnfg.waypoint.authorization.utility.password;

import java.util.regex.Pattern;

public class AtLeastNumberStrategy extends PasswordValidationStrategy {

  @Override
  String getRequirementType() {
    return "NUMBER";
  }

  @Override
  String getRequirementMessage() {
    return "Password must contain a number";
  }

  @Override
  public boolean isValid(String password) {
    // \d matches against a digit [0-9]
    return Pattern.compile("\\d").matcher(password).find();
  }

}
