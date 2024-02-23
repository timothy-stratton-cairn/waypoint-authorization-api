package com.cairnfg.waypoint.authorization.utility.password;

import com.cairnfg.waypoint.authorization.dto.RequirementDto;
import com.cairnfg.waypoint.authorization.dto.Status;
import org.apache.commons.lang3.StringUtils;

public abstract class PasswordValidationStrategy {

  abstract String getRequirementType();

  abstract String getRequirementMessage();

  abstract boolean isValid(String password);

  public RequirementDto getValidationResult(String password) {
    return RequirementDto.getRequirement(getRequirementType(),
        StringUtils.isNotEmpty(password) && isValid(password) ? Status.PASSED : Status.FAILED,
        getRequirementMessage());
  }
}
