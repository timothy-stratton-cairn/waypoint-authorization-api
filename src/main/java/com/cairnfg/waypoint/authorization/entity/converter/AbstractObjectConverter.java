package com.cairnfg.waypoint.authorization.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class AbstractObjectConverter<T> implements AttributeConverter<T, String> {

  ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(T object) {
    if (object == null) {
      return "";
    }

    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      return "";
    }
  }

  @Override
  public T convertToEntityAttribute(String stringJson) {
    try {
      return mapper.readValue(stringJson, new TypeReference<T>() {
      });
    } catch (JsonProcessingException e) {
      log.debug("Error parsing JSON. Initializing with empty collection.");
      return null;
    } catch (IllegalArgumentException e) {
      log.debug("No value present in the Database. Initializing with empty collection.");
      return null;
    }
  }
}
