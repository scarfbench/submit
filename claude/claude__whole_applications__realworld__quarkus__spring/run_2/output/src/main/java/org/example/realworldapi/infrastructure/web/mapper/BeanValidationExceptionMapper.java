package org.example.realworldapi.infrastructure.web.mapper;

import jakarta.validation.ConstraintViolationException;
import org.example.realworldapi.application.web.model.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BeanValidationExceptionMapper {

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
    ErrorResponse errorResponse = new ErrorResponse();
    e.getConstraintViolations()
        .iterator()
        .forEachRemaining(constraint -> errorResponse.getBody().add(constraint.getMessage()));
    return ResponseEntity.status(422).body(errorResponse);
  }
}
