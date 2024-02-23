package com.cairnfg.waypoint.authorization.utility.validation;

import com.cairnfg.waypoint.authorization.utility.PasswordUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    try {
      return PasswordUtility.isPasswordValid(value);
    } catch (JsonProcessingException e) {
      return false;
    }
  }

}
