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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(@Qualifier("wrappingObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<String> handleArticleNotFound(ArticleNotFoundException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(TagNotFoundException.class)
    public ResponseEntity<String> handleTagNotFound(TagNotFoundException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<String> handleCommentNotFound(CommentNotFoundException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler({EmailAlreadyExistsException.class, UsernameAlreadyExistsException.class})
    public ResponseEntity<String> handleConflict(BusinessException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<String> handleInvalidPassword(InvalidPasswordException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(ModelValidationException.class)
    public ResponseEntity<String> handleModelValidation(ModelValidationException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(new ErrorResponse(ex.getMessages())));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(new ErrorResponse("Unauthorized")));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleForbidden(ForbiddenException ex) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(new ErrorResponse("Forbidden")));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse();
        ex.getConstraintViolations().forEach(violation -> errorResponse.getBody().add(violation.getMessage()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(errorResponse));
    }
}
