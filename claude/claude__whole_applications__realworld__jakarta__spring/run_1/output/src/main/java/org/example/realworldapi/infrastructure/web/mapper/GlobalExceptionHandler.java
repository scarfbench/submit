package org.example.realworldapi.infrastructure.web.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.example.realworldapi.application.web.model.response.ErrorResponse;
import org.example.realworldapi.domain.exception.*;
import org.example.realworldapi.infrastructure.web.exception.ForbiddenException;
import org.example.realworldapi.infrastructure.web.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(@Qualifier("wrapRootValueObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(objectMapper.writeValueAsString(new ErrorResponse(HttpStatus.UNAUTHORIZED.toString())));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleForbidden(ForbiddenException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(objectMapper.writeValueAsString(new ErrorResponse(HttpStatus.FORBIDDEN.toString())));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<String> handleArticleNotFound(ArticleNotFoundException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(TagNotFoundException.class)
    public ResponseEntity<String> handleTagNotFound(TagNotFoundException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<String> handleCommentNotFound(CommentNotFoundException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailExists(EmailAlreadyExistsException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<String> handleUsernameExists(UsernameAlreadyExistsException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<String> handleInvalidPassword(InvalidPasswordException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(ModelValidationException.class)
    public ResponseEntity<String> handleModelValidation(ModelValidationException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse();
        ex.getConstraintViolations().forEach(cv -> errorResponse.getBody().add(cv.getMessage()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(objectMapper.writeValueAsString(errorResponse));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse();
        ex.getBindingResult().getAllErrors().forEach(error -> errorResponse.getBody().add(error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(objectMapper.writeValueAsString(errorResponse));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(objectMapper.writeValueAsString(new ErrorResponse("request body must be not null")));
    }
}
