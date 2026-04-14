package org.eclipse.cargotracker.application.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Jackson ObjectMapper context resolver that configures proper date/time handling
 * for JAX-RS endpoints. Uses Jackson instead of JSON-B for better LocalDateTime support.
 */
@Provider
public class JsonbContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JsonbContextResolver() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
