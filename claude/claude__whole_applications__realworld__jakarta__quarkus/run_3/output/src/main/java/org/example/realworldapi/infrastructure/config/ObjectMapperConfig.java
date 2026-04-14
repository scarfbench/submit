package org.example.realworldapi.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class ObjectMapperConfig implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper objectMapper) {
        // Register JavaTimeModule for LocalDateTime serialization
        objectMapper.registerModule(new JavaTimeModule());
    }
}
