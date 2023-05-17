package org.camunda.custom.operate.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.dto.Variable;
import java.io.IOException;
import java.util.List;
import javax.persistence.AttributeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonVariableConverter implements AttributeConverter<List<Variable>, String> {

  private final Logger logger = LoggerFactory.getLogger(JsonVariableConverter.class);

  private static ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(List<Variable> attribute) {

    String json = null;
    try {
      json = objectMapper.writeValueAsString(attribute);
    } catch (final JsonProcessingException e) {
      logger.error("JSON writing error", e);
    }

    return json;
  }

  @Override
  public List<Variable> convertToEntityAttribute(String json) {
    if (json == null) {
      return null;
    }
    List<Variable> attribute = null;
    try {
      attribute = objectMapper.readValue(json, new TypeReference<List<Variable>>() {});
    } catch (final IOException e) {
      logger.error("JSON reading error", e);
    }

    return attribute;
  }
}
