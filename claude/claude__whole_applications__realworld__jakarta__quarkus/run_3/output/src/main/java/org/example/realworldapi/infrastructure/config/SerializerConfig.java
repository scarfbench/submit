package org.example.realworldapi.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Singleton;

@Singleton
public class SerializerConfig {

    private final ObjectMapper wrappingObjectMapper;
    private final ObjectMapper noWrapRootValueObjectMapper;

    public SerializerConfig() {
        this.wrappingObjectMapper = new ObjectMapper();
        this.wrappingObjectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        this.wrappingObjectMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        this.wrappingObjectMapper.registerModule(new JavaTimeModule());

        this.noWrapRootValueObjectMapper = new ObjectMapper();
        this.noWrapRootValueObjectMapper.registerModule(new JavaTimeModule());
    }

    public ObjectMapper getWrappingObjectMapper() {
        return wrappingObjectMapper;
    }

    public ObjectMapper getNoWrapRootValueObjectMapper() {
        return noWrapRootValueObjectMapper;
    }
}
