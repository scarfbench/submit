package com.coffeeshop.orders.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ConstraintViolationExceptionMapper {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, List<Map<String, String>>>> handleConstraintViolation(
            ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
            .map(this::toEntry)
            .toList();
        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }

    private Map<String, String> toEntry(ConstraintViolation<?> v) {
        String field = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
        return Map.of("field", field, "message", v.getMessage());
    }
}
