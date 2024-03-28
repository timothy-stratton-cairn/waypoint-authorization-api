package com.cairnfg.waypoint.authorization.entity.converter;

import com.cairnfg.waypoint.authorization.configuration.cryptography.AES256;
import jakarta.persistence.AttributeConverter;
import org.springframework.beans.factory.annotation.Value;

public class EncryptedFieldConverter implements AttributeConverter<String, String> {

  private final String secretKey;
  private final String salt;
  private final Boolean enabled;

  public EncryptedFieldConverter(
      @Value("${waypoint.authorization.encrypted-field.secret-key}") String secretKey,
      @Value("${waypoint.authorization.encrypted-field.salt}") String salt,
      @Value("${waypoint.authorization.encrypted-field.enabled}") Boolean enabled) {
    this.secretKey = secretKey;
    this.salt = salt;
    this.enabled = enabled;
  }

  @Override
  public String convertToDatabaseColumn(String s) {
    if (enabled && s != null && !s.isBlank()) {
      return AES256.encrypt(s, secretKey, salt);
    }
    return s;
  }

  @Override
  public String convertToEntityAttribute(String s) {
    if (enabled && s != null && !s.isBlank()) {
      return AES256.decrypt(s, secretKey, salt);
    }
    return s;
  }
}
