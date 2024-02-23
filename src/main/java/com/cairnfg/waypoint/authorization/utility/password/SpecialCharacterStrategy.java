package com.cairnfg.waypoint.authorization.utility.password;

import java.util.regex.Pattern;

public class SpecialCharacterStrategy extends PasswordValidationStrategy {

  @Override
  public boolean isValid(String password) {
    return Pattern.compile("[!.,:;'\"^@#$%&*()_+=|<>?{}/\\[\\]~-]").matcher(password).find();
  }

  @Override
  String getRequirementType() {
    return "SPECIAL_CHARACTER";
  }

  @Override
  String getRequirementMessage() {
    return "Password must contain a special character";
  }

}
