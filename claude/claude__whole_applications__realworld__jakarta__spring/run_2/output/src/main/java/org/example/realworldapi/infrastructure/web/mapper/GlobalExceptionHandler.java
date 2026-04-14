package org.example.realworldapi.infrastructure.web.mapper;

import jakarta.validation.ConstraintViolationException;
import org.example.realworldapi.application.web.model.response.ErrorResponse;
import org.example.realworldapi.domain.exception.*;
import org.example.realworldapi.infrastructure.web.exception.ForbiddenException;
import org.example.realworldapi.infrastructure.web.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, ErrorResponse>> errorResponse(int status, ErrorResponse errorResponse) {
        return ResponseEntity.status(status).body(Collections.singletonMap("errors", errorResponse));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleConstraintViolation(ConstraintViolationException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        e.getConstraintViolations().forEach(v -> errorResponse.getBody().add(v.getMessage()));
        return errorResponse(422, errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        e.getBindingResult().getAllErrors().forEach(error -> errorResponse.getBody().add(error.getDefaultMessage()));
        return errorResponse(422, errorResponse);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleEmailExists(EmailAlreadyExistsException e) {
        return errorResponse(HttpStatus.CONFLICT.value(), new ErrorResponse(e.getMessages()));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleUsernameExists(UsernameAlreadyExistsException e) {
        return errorResponse(HttpStatus.CONFLICT.value(), new ErrorResponse(e.getMessages()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleUserNotFound(UserNotFoundException e) {
        return errorResponse(HttpStatus.NOT_FOUND.value(), new ErrorResponse(e.getMessages()));
    }

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleArticleNotFound(ArticleNotFoundException e) {
        return errorResponse(HttpStatus.NOT_FOUND.value(), new ErrorResponse(e.getMessages()));
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleCommentNotFound(CommentNotFoundException e) {
        return errorResponse(HttpStatus.NOT_FOUND.value(), new ErrorResponse(e.getMessages()));
    }

    @ExceptionHandler(TagNotFoundException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleTagNotFound(TagNotFoundException e) {
        return errorResponse(HttpStatus.NOT_FOUND.value(), new ErrorResponse(e.getMessages()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleInvalidPassword(InvalidPasswordException e) {
        return errorResponse(HttpStatus.UNAUTHORIZED.value(), new ErrorResponse(e.getMessages()));
    }

    @ExceptionHandler(ModelValidationException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleModelValidation(ModelValidationException e) {
        return errorResponse(422, new ErrorResponse(e.getMessages()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleUnauthorized(UnauthorizedException e) {
        return errorResponse(HttpStatus.UNAUTHORIZED.value(), new ErrorResponse(HttpStatus.UNAUTHORIZED.toString()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, ErrorResponse>> handleForbidden(ForbiddenException e) {
        return errorResponse(HttpStatus.FORBIDDEN.value(), new ErrorResponse(HttpStatus.FORBIDDEN.toString()));
    }
}
