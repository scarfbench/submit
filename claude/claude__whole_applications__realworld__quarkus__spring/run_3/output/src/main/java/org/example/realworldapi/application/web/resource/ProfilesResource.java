package org.example.realworldapi.application.web.resource;

import java.security.Principal;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.FollowUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
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

    public ProfilesResource(FollowUserByUsername followUserByUsername,
                            UnfollowUserByUsername unfollowUserByUsername,
                            ResourceUtils resourceUtils) {
        this.followUserByUsername = followUserByUsername;
        this.unfollowUserByUsername = unfollowUserByUsername;
        this.resourceUtils = resourceUtils;
    }

    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getProfile(
            @PathVariable String username,
            Principal principal) {
        final var loggedUserId = resourceUtils.getLoggedUserId(principal);
        final var profileResponse = resourceUtils.profileResponse(username, loggedUserId);
        return ResponseEntity.ok(profileResponse);
    }

    @PostMapping(value = "/{username}/follow", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Object> follow(
            @PathVariable String username,
            Principal principal) {
        final var loggedUserId = resourceUtils.getLoggedUserId(principal);
        followUserByUsername.handle(loggedUserId, username);
        return ResponseEntity.ok(resourceUtils.profileResponse(username, loggedUserId));
    }

    @DeleteMapping(value = "/{username}/follow", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Object> unfollow(
            @PathVariable String username,
            Principal principal) {
        final var loggedUserId = resourceUtils.getLoggedUserId(principal);
        unfollowUserByUsername.handle(loggedUserId, username);
        return ResponseEntity.ok(resourceUtils.profileResponse(username, loggedUserId));
    }
}
