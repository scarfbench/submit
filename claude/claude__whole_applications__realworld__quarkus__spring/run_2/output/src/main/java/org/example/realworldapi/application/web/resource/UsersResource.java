package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.realworldapi.application.web.model.request.LoginRequest;
import org.example.realworldapi.application.web.model.request.NewUserRequest;
import org.example.realworldapi.application.web.model.response.UserResponse;
import org.example.realworldapi.domain.exception.InvalidPasswordException;
import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.feature.CreateUser;
import org.example.realworldapi.domain.feature.LoginUser;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.infrastructure.provider.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UsersResource {

  private final CreateUser createUser;
  private final LoginUser loginUser;
  private final JwtTokenProvider tokenProvider;
  private final ObjectMapper wrapMapper;

  public UsersResource(
      CreateUser createUser,
      LoginUser loginUser,
      JwtTokenProvider tokenProvider,
      @Qualifier("wrapRootValueObjectMapper") ObjectMapper wrapMapper) {
    this.createUser = createUser;
    this.loginUser = loginUser;
    this.tokenProvider = tokenProvider;
    this.wrapMapper = wrapMapper;
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<String> create(
      @Valid @RequestBody @NotNull(message = ValidationMessages.REQUEST_BODY_MUST_BE_NOT_NULL)
          NewUserRequest newUserRequest)
      throws JsonProcessingException {
    final var user = createUser.handle(newUserRequest.toCreateUserInput());
    final var token = tokenProvider.createUserToken(user.getId().toString());
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(new UserResponse(user, token)));
  }

  @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> login(
      @Valid @RequestBody @NotNull(message = ValidationMessages.REQUEST_BODY_MUST_BE_NOT_NULL)
          LoginRequest loginRequest)
      throws JsonProcessingException {
    User user;
    try {
      user = loginUser.handle(loginRequest.toLoginUserInput());
    } catch (UserNotFoundException | InvalidPasswordException ex) {
      throw new BadCredentialsException("Invalid email or password");
    }
    final var token = tokenProvider.createUserToken(user.getId().toString());
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(new UserResponse(user, token)));
  }
}
