package org.eclipse.cargotracker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Application configuration for common beans.
 */
@ApplicationScoped
public class AppConfig {

  @Produces
  @ApplicationScoped
  public ObjectMapper objectMapper() {
    return JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .build();
  }
}
