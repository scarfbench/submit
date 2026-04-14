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

    @Bean(name = "wrappingObjectMapper")
    public ObjectMapper wrappingObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        objectMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean(name = "noWrapRootValueObjectMapper")
    @Primary
    public ObjectMapper noWrapRootValueObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
