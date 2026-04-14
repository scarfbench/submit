package org.example.realworldapi.infrastructure.web.mapper;

/**
 * Exception mapping is now handled by
 * org.example.realworldapi.infrastructure.web.provider.BusinessExceptionMapper
 * and org.example.realworldapi.infrastructure.web.provider.ValidationExceptionMapper.
 *
 * This class was previously used with Quarkus @ServerExceptionMapper annotations
 * which are not available in standard Jakarta EE / RESTEasy.
 */
public class ExceptionMappers {
    // Intentionally empty - exception mapping moved to JAX-RS ExceptionMapper providers
}
