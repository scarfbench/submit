package org.example.realworldapi.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Singleton;

/**
 * Provides pre-configured ObjectMapper instances for manual serialization.
 * These are NOT CDI producers to avoid interfering with Quarkus's default ObjectMapper.
 */
@Singleton
public class SerializerConfig {

    private final ObjectMapper wrapRootValueMapper;
    private final ObjectMapper noWrapRootValueMapper;

    public SerializerConfig() {
        this.wrapRootValueMapper = new ObjectMapper();
        this.wrapRootValueMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        this.wrapRootValueMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        this.wrapRootValueMapper.registerModule(new JavaTimeModule());

        this.noWrapRootValueMapper = new ObjectMapper();
        this.noWrapRootValueMapper.registerModule(new JavaTimeModule());
    }

    public ObjectMapper getWrapRootValueMapper() {
        return wrapRootValueMapper;
    }

    public ObjectMapper getNoWrapRootValueMapper() {
        return noWrapRootValueMapper;
    }
}
