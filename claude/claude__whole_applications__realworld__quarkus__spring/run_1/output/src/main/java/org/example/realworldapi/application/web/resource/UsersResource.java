package org.example.realworldapi.application.web.resource;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.realworldapi.application.web.model.request.LoginRequest;
import org.example.realworldapi.application.web.model.request.NewUserRequest;
import org.example.realworldapi.application.web.model.response.UserResponse;
import org.example.realworldapi.domain.exception.InvalidPasswordException;
import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.feature.CreateUser;
import org.example.realworldapi.domain.feature.LoginUser;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.infrastructure.provider.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UsersResource {

  private final CreateUser createUser;
  private final LoginUser loginUser;
  private final JwtTokenProvider tokenProvider;

  @PostMapping
  @Transactional
  public ResponseEntity<UserResponse> create(
      @Valid @RequestBody NewUserRequest newUserRequest) {
    final var user = createUser.handle(newUserRequest.toCreateUserInput());
    final var token = tokenProvider.createUserToken(user.getId().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(user, token));
  }

  @PostMapping("/login")
  public ResponseEntity<UserResponse> login(
      @Valid @RequestBody LoginRequest loginRequest) {
    User user;
    try {
      user = loginUser.handle(loginRequest.toLoginUserInput());
    } catch (UserNotFoundException | InvalidPasswordException ex) {
      throw new BadCredentialsException("Invalid credentials");
    }
    final var token = tokenProvider.createUserToken(user.getId().toString());
    return ResponseEntity.ok(new UserResponse(user, token));
  }
}
