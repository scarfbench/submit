package org.example.realworldapi.infrastructure.web.mapper;

import org.example.realworldapi.application.web.model.response.ErrorResponse;
import org.example.realworldapi.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionMappers {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> unauthorized(BadCredentialsException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse("Unauthorized"));
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> emailAlreadyExists(EmailAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse(e));
  }

  @ExceptionHandler(UsernameAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> usernameAlreadyExists(UsernameAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse(e));
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> userNotfound(UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(e));
  }

  @ExceptionHandler(InvalidPasswordException.class)
  public ResponseEntity<ErrorResponse> invalidPassword(InvalidPasswordException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse(e));
  }

  @ExceptionHandler(TagNotFoundException.class)
  public ResponseEntity<ErrorResponse> tagNotFound(TagNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(e));
  }

  @ExceptionHandler(ArticleNotFoundException.class)
  public ResponseEntity<ErrorResponse> articleNotFound(ArticleNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(e));
  }

  @ExceptionHandler(ModelValidationException.class)
  public ResponseEntity<ErrorResponse> modelValidation(ModelValidationException e) {
    return ResponseEntity.status(422).body(errorResponse(e));
  }

  private ErrorResponse errorResponse(String e) {
    return new ErrorResponse(e);
  }

  private ErrorResponse errorResponse(RuntimeException e) {
    return new ErrorResponse(e.getMessage());
  }
}
