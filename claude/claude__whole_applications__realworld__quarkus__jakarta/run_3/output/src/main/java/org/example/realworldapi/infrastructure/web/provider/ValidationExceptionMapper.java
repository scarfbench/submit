package org.example.realworldapi.infrastructure.web.provider;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String[] messages = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toArray(String[]::new);

        return Response.status(422)
                .entity(Map.of("errors", Map.of("body", messages)))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
