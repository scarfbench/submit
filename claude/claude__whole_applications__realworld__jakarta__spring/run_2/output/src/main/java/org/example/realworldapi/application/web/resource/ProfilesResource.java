package org.example.realworldapi.application.web.resource;

import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.FollowUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfilesResource {

    @Autowired
    private FollowUserByUsername followUserByUsername;
    @Autowired
    private UnfollowUserByUsername unfollowUserByUsername;
    @Autowired
    private ResourceUtils resourceUtils;

    @GetMapping(value = "/{username}", produces = "application/json")
    public ResponseEntity<?> getProfile(
            @PathVariable String username,
            @RequestAttribute(required = false) UUID loggedUserId) {
        final var profileResponse = resourceUtils.profileResponse(username, loggedUserId);
        return ResponseEntity.ok(Collections.singletonMap("profile", profileResponse));
    }

    @PostMapping(value = "/{username}/follow", produces = "application/json")
    @Transactional
    public ResponseEntity<?> follow(
            @PathVariable String username,
            @RequestAttribute UUID loggedUserId) {
        followUserByUsername.handle(loggedUserId, username);
        return ResponseEntity.ok(Collections.singletonMap("profile", resourceUtils.profileResponse(username, loggedUserId)));
    }

    @DeleteMapping(value = "/{username}/follow", produces = "application/json")
    @Transactional
    public ResponseEntity<?> unfollow(
            @PathVariable String username,
            @RequestAttribute UUID loggedUserId) {
        unfollowUserByUsername.handle(loggedUserId, username);
        return ResponseEntity.ok(Collections.singletonMap("profile", resourceUtils.profileResponse(username, loggedUserId)));
    }
}
