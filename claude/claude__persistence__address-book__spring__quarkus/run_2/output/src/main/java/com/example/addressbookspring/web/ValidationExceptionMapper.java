package com.example.addressbookspring.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps validation constraint violations to HTTP 400 responses.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Validation failed");
        error.put("violations", exception.getConstraintViolations().stream()
                .map(v -> {
                    Map<String, String> violation = new HashMap<>();
                    violation.put("field", v.getPropertyPath().toString());
                    violation.put("message", v.getMessage());
                    return violation;
                })
                .collect(Collectors.toList()));
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
