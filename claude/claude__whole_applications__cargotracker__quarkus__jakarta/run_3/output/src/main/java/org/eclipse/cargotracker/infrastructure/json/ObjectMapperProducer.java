package org.eclipse.cargotracker.infrastructure.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * CDI producer for Jackson ObjectMapper.
 * In Quarkus this was auto-provided; in standard Jakarta EE we need to produce it.
 */
@ApplicationScoped
public class ObjectMapperProducer {

  @Produces
  @ApplicationScoped
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }
}
