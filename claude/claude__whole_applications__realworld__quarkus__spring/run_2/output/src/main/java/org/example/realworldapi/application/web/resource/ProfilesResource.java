package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import java.security.Principal;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.FollowUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
public class ProfilesResource {

  private final FollowUserByUsername followUserByUsername;
  private final UnfollowUserByUsername unfollowUserByUsername;
  private final ResourceUtils resourceUtils;
  private final ObjectMapper wrapMapper;

  public ProfilesResource(
      FollowUserByUsername followUserByUsername,
      UnfollowUserByUsername unfollowUserByUsername,
      ResourceUtils resourceUtils,
      @Qualifier("wrapRootValueObjectMapper") ObjectMapper wrapMapper) {
    this.followUserByUsername = followUserByUsername;
    this.unfollowUserByUsername = unfollowUserByUsername;
    this.resourceUtils = resourceUtils;
    this.wrapMapper = wrapMapper;
  }

  @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getProfile(
      @PathVariable @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK)
          String username,
      Principal principal)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var profileResponse = resourceUtils.profileResponse(username, loggedUserId);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(profileResponse));
  }

  @PostMapping(value = "/{username}/follow", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<String> follow(
      @PathVariable @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK)
          String username,
      Principal principal)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    followUserByUsername.handle(loggedUserId, username);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(resourceUtils.profileResponse(username, loggedUserId)));
  }

  @DeleteMapping(value = "/{username}/follow", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<String> unfollow(
      @PathVariable @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK)
          String username,
      Principal principal)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    unfollowUserByUsername.handle(loggedUserId, username);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(resourceUtils.profileResponse(username, loggedUserId)));
  }
}
