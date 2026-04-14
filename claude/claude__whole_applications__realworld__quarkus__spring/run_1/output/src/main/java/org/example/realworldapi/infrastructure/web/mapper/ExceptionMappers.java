package org.example.realworldapi.infrastructure.web.mapper;

import jakarta.validation.ConstraintViolationException;
import org.example.realworldapi.application.web.model.response.ErrorResponse;
import org.example.realworldapi.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionMappers {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> unauthorized(BadCredentialsException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse("Unauthorized"));
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> emailAlreadyExists(EmailAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse(e.getMessage()));
  }

  @ExceptionHandler(UsernameAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> usernameAlreadyExists(UsernameAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse(e.getMessage()));
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> userNotFound(UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(e.getMessage()));
  }

  @ExceptionHandler(InvalidPasswordException.class)
  public ResponseEntity<ErrorResponse> invalidPassword(InvalidPasswordException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse(e.getMessage()));
  }

  @ExceptionHandler(TagNotFoundException.class)
  public ResponseEntity<ErrorResponse> tagNotFound(TagNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(e.getMessage()));
  }

  @ExceptionHandler(ArticleNotFoundException.class)
  public ResponseEntity<ErrorResponse> articleNotFound(ArticleNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(e.getMessage()));
  }

  @ExceptionHandler(ModelValidationException.class)
  public ResponseEntity<ErrorResponse> modelValidation(ModelValidationException e) {
    return ResponseEntity.status(422)
        .body(new ErrorResponse(e.getMessage()));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> constraintViolation(ConstraintViolationException e) {
    ErrorResponse errorResponse = new ErrorResponse();
    e.getConstraintViolations()
        .iterator()
        .forEachRemaining(constraint -> errorResponse.getBody().add(constraint.getMessage()));
    return ResponseEntity.status(422).body(errorResponse);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> methodArgumentNotValid(MethodArgumentNotValidException e) {
    ErrorResponse errorResponse = new ErrorResponse();
    e.getBindingResult().getAllErrors()
        .forEach(error -> errorResponse.getBody().add(error.getDefaultMessage()));
    return ResponseEntity.status(422).body(errorResponse);
  }
}
