package com.cairnfg.waypoint.authorization.entity.converter;

import com.cairnfg.waypoint.authorization.configuration.cryptography.AES256;
import jakarta.persistence.AttributeConverter;
import org.springframework.beans.factory.annotation.Value;

public class EncryptedFieldConverter implements AttributeConverter<String, String> {

  private final String secretKey;
  private final String salt;

  public EncryptedFieldConverter(
      @Value("${waypoint.authorization.encrypted-field.secret-key}") String secretKey,
      @Value("${waypoint.authorization.encrypted-field.salt}") String salt) {
    this.secretKey = secretKey;
    this.salt = salt;
  }

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
