package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.security.Principal;
import java.util.UUID;
import org.example.realworldapi.application.web.model.request.UpdateUserRequest;
import org.example.realworldapi.application.web.model.response.UserResponse;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.UpdateUser;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.infrastructure.provider.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserResource {

  private final FindUserById findUserById;
  private final UpdateUser updateUser;
  private final JwtTokenProvider tokenProvider;
  private final ObjectMapper wrapMapper;

  public UserResource(
      FindUserById findUserById,
      UpdateUser updateUser,
      JwtTokenProvider tokenProvider,
      @Qualifier("wrapRootValueObjectMapper") ObjectMapper wrapMapper) {
    this.findUserById = findUserById;
    this.updateUser = updateUser;
    this.tokenProvider = tokenProvider;
    this.wrapMapper = wrapMapper;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getUser(Principal principal) throws JsonProcessingException {
    final var userId = UUID.fromString(principal.getName());
    final var user = findUserById.handle(userId);
    final var token = tokenProvider.createUserToken(user.getId().toString());
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(new UserResponse(user, token)));
  }

  @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<String> update(
      Principal principal,
      @Valid @RequestBody @NotNull(message = ValidationMessages.REQUEST_BODY_MUST_BE_NOT_NULL)
          UpdateUserRequest updateUserRequest)
      throws JsonProcessingException {
    final var userId = UUID.fromString(principal.getName());
    final var user = updateUser.handle(updateUserRequest.toUpdateUserInput(userId));
    final var token = tokenProvider.createUserToken(user.getId().toString());
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(new UserResponse(user, token)));
  }
}
