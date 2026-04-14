package org.example.realworldapi.infrastructure.web.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JacksonConfigProvider implements ContextResolver<ObjectMapper> {

  private final ObjectMapper objectMapper;

  public JacksonConfigProvider() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    this.objectMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }
}
