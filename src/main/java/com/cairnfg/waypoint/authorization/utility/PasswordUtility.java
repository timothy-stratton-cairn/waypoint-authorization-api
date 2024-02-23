package com.cairnfg.waypoint.authorization.utility;

import com.cairnfg.waypoint.authorization.dto.PasswordResponseDto;
import com.cairnfg.waypoint.authorization.dto.RequirementDto;
import com.cairnfg.waypoint.authorization.dto.Status;
import com.cairnfg.waypoint.authorization.exception.PasswordValidationException;
import com.cairnfg.waypoint.authorization.utility.password.AtLeastLetterStrategy;
import com.cairnfg.waypoint.authorization.utility.password.AtLeastNumberStrategy;
import com.cairnfg.waypoint.authorization.utility.password.IsEmptyStrategy;
import com.cairnfg.waypoint.authorization.utility.password.MinLengthStrategy;
import com.cairnfg.waypoint.authorization.utility.password.SpecialCharacterStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;

public class PasswordUtility {

  private static final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
  private static final SecureRandom SECURE_RANDOM;
  private static final int DEF_COUNT = 20;

  static {
    SECURE_RANDOM = new SecureRandom();
    SECURE_RANDOM.nextBytes(new byte[64]);
  }

  /**
   * This is a utility class and is used in IT, throwing an IllegalStateException will break IT so
   * do nothing.
   */
  private PasswordUtility() {
  }

  public static PasswordResponseDto validatePassword(String potentialPassword) {
    List<RequirementDto> requirementList = new ArrayList<>();
    requirementList.add(new IsEmptyStrategy().getValidationResult(potentialPassword));
    requirementList.add(new MinLengthStrategy().getValidationResult(potentialPassword));
    requirementList.add(new AtLeastLetterStrategy().getValidationResult(potentialPassword));
    requirementList.add(new AtLeastNumberStrategy().getValidationResult(potentialPassword));
    requirementList.add(new SpecialCharacterStrategy().getValidationResult(potentialPassword));

    return requirementList.stream()
        .anyMatch(requirementDto -> requirementDto.getStatus().equals(Status.FAILED))
        ? new PasswordResponseDto(Status.FAILED, requirementList)
        : new PasswordResponseDto(Status.PASSED, requirementList);
  }


  public static String generateRandomAlphanumericString() {
    return RandomStringUtils.random(DEF_COUNT, 0, 0, true, true, null, SECURE_RANDOM);
  }

  public static boolean isPasswordValid(String potentialPassword)
      throws PasswordValidationException, JsonProcessingException {
    PasswordResponseDto validationResponse = validatePassword(potentialPassword);
    if (validationResponse.getOverallStatus().equals(Status.FAILED)) {
      throw new PasswordValidationException(
          String.format("The given password [%s] fails validation [%s]", potentialPassword,
              ow.writeValueAsString(validationResponse)),
          validationResponse.getRequirements().stream()
              .filter(requirementDto -> requirementDto.getStatus().equals(Status.FAILED))
              .map(RequirementDto::getMessage).collect(Collectors.joining(", ")));
    }

    return validationResponse.getOverallStatus().equals(Status.PASSED);
  }
}
