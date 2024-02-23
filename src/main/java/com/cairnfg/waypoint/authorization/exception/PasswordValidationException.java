package com.cairnfg.waypoint.authorization.exception;

import lombok.Getter;

@Getter
public class PasswordValidationException extends RuntimeException {

  private static final long serialVersionUID = -8316938025205926271L;

  private final String requirementsMessage;

  public PasswordValidationException(String errorMessage) {
    super(errorMessage);
    this.requirementsMessage = errorMessage;
  }

  public PasswordValidationException(String errorMessage, String requirementsMessage) {
    super(errorMessage);
    this.requirementsMessage = requirementsMessage;
  }
}
