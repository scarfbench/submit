package org.example.realworldapi.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.example.realworldapi.infrastructure.web.qualifiers.NoWrapRootValueObjectMapper;
import org.example.realworldapi.infrastructure.web.qualifiers.WrapRootValueObjectMapper;

@ApplicationScoped
public class SerializerConfig {

    /**
     * Register JavaTimeModule on the Quarkus-managed default ObjectMapper.
     * The default ObjectMapper must NOT have WRAP_ROOT_VALUE / UNWRAP_ROOT_VALUE
     * because RESTEasy uses it for request deserialization and response serialization.
     */
    @Singleton
    public static class DefaultObjectMapperCustomizer implements ObjectMapperCustomizer {
        @Override
        public void customize(ObjectMapper objectMapper) {
            objectMapper.registerModule(new JavaTimeModule());
        }
    }

    /**
     * ObjectMapper with WRAP_ROOT_VALUE enabled. Used for manually serializing
     * responses that have @JsonRootName annotations (e.g. UserResponse, ArticleResponse).
     */
    @Singleton
    @Produces
    @WrapRootValueObjectMapper
    public ObjectMapper wrapRootValueObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        objectMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    /**
     * ObjectMapper without WRAP_ROOT_VALUE. Used for manually serializing
     * list/collection responses (ArticlesResponse, CommentsResponse).
     */
    @Singleton
    @Produces
    @NoWrapRootValueObjectMapper
    public ObjectMapper noWrapRootValueObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

}
