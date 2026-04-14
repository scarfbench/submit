package org.example.realworldapi.application.web.resource;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.example.realworldapi.application.web.model.request.UpdateUserRequest;
import org.example.realworldapi.application.web.model.response.UserResponse;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.UpdateUser;
import org.example.realworldapi.infrastructure.provider.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserResource {

  private final FindUserById findUserById;
  private final UpdateUser updateUser;
  private final JwtTokenProvider tokenProvider;

  @GetMapping
  public ResponseEntity<UserResponse> getUser(Principal principal) {
    final var userId = UUID.fromString(principal.getName());
    final var user = findUserById.handle(userId);
    final var token = tokenProvider.createUserToken(user.getId().toString());
    return ResponseEntity.ok(new UserResponse(user, token));
  }

  @PutMapping
  @Transactional
  public ResponseEntity<UserResponse> update(
      Principal principal,
      @Valid @RequestBody UpdateUserRequest updateUserRequest) {
    final var userId = UUID.fromString(principal.getName());
    final var user = updateUser.handle(updateUserRequest.toUpdateUserInput(userId));
    final var token = tokenProvider.createUserToken(user.getId().toString());
    return ResponseEntity.ok(new UserResponse(user, token));
  }
}
