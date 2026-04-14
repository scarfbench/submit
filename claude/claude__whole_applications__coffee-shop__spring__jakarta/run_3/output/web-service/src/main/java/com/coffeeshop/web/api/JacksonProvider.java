package com.coffeeshop.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Jackson ObjectMapper provider for JAX-RS.
 * Configures Jackson with Java 8 and Java Time module support,
 * replacing the Spring Boot auto-configured ObjectMapper.
 */
@Provider
public class JacksonProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;

    public JacksonProvider() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
