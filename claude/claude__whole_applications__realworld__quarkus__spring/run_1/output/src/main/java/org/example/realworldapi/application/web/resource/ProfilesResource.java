package org.example.realworldapi.application.web.resource;

import java.security.Principal;
import lombok.AllArgsConstructor;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.FollowUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@AllArgsConstructor
public class ProfilesResource {

  private final FollowUserByUsername followUserByUsername;
  private final UnfollowUserByUsername unfollowUserByUsername;
  private final ResourceUtils resourceUtils;

  @GetMapping("/{username}")
  public ResponseEntity<Object> getProfile(
      @PathVariable String username, Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var profileResponse = resourceUtils.profileResponse(username, loggedUserId);
    return ResponseEntity.ok(profileResponse);
  }

  @PostMapping("/{username}/follow")
  @Transactional
  public ResponseEntity<Object> follow(
      @PathVariable String username, Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    followUserByUsername.handle(loggedUserId, username);
    return ResponseEntity.ok(resourceUtils.profileResponse(username, loggedUserId));
  }

  @DeleteMapping("/{username}/follow")
  @Transactional
  public ResponseEntity<Object> unfollow(
      @PathVariable String username, Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    unfollowUserByUsername.handle(loggedUserId, username);
    return ResponseEntity.ok(resourceUtils.profileResponse(username, loggedUserId));
  }
}
