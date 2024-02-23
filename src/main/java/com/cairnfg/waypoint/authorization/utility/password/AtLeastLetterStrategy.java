package com.cairnfg.waypoint.authorization.utility.password;

import java.util.regex.Pattern;

public class AtLeastLetterStrategy extends PasswordValidationStrategy {

  @Override
  String getRequirementType() {
    return "LETTER";
  }

  @Override
  String getRequirementMessage() {
    return "Password must contain a letter";
  }

  @Override
  boolean isValid(String password) {
    return Pattern.compile("[a-zA-Z]").matcher(password).find();
  }

}
