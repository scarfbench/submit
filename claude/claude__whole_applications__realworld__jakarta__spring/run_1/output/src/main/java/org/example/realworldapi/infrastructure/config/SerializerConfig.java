package org.example.realworldapi.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SerializerConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        // Primary ObjectMapper used by Spring MVC for request deserialization - NO root wrapping
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean("wrapRootValueObjectMapper")
    public ObjectMapper wrapRootValueObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        objectMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean("noWrapRootValueObjectMapper")
    public ObjectMapper noWrapRootValueObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
