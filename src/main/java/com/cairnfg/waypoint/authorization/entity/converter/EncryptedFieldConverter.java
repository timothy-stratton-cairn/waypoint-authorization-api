package com.cairnfg.waypoint.authorization.entity.converter;

import com.cairnfg.waypoint.authorization.configuration.cryptography.AES256;
import jakarta.persistence.AttributeConverter;

public class EncryptedFieldConverter implements AttributeConverter<String, String> {

  //TODO These secrets should be stored in a secure place
  private final String secretKey = "ldcAQwZs6EWmWJC#GVsx%*1s3RJfoabp%mfa@%9&BiDwQRjT$0qEvqLrj3jU2F0j$DHN5xVOGlQ*NUILqHZQparn8zWCVaM9GPB";
  private final String salt = "zS95&#$yYWDLBUMM";

  @Override
  public String convertToDatabaseColumn(String s) {
    if (s != null && !s.isBlank()) {
      return AES256.encrypt(s, secretKey, salt);
    }
    return null;
  }

  @Override
  public String convertToEntityAttribute(String s) {
    if (s != null && !s.isBlank()) {
      return AES256.decrypt(s, secretKey, salt);
    }
    return null;
  }
}
