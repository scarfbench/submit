package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.FollowUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
public class ProfilesResource {

    private final FollowUserByUsername followUserByUsername;
    private final UnfollowUserByUsername unfollowUserByUsername;
    private final ResourceUtils resourceUtils;
    private final ObjectMapper objectMapper;

    public ProfilesResource(
            FollowUserByUsername followUserByUsername,
            UnfollowUserByUsername unfollowUserByUsername,
            ResourceUtils resourceUtils,
            @Qualifier("wrappingObjectMapper") ObjectMapper objectMapper) {
        this.followUserByUsername = followUserByUsername;
        this.unfollowUserByUsername = unfollowUserByUsername;
        this.resourceUtils = resourceUtils;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProfile(@PathVariable String username) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        final var profileResponse = resourceUtils.profileResponse(username, loggedUserId);
        return ResponseEntity.ok(objectMapper.writeValueAsString(profileResponse));
    }

    @PostMapping(value = "/{username}/follow", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> follow(@PathVariable String username) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        followUserByUsername.handle(loggedUserId, username);
        return ResponseEntity.ok(objectMapper.writeValueAsString(
                resourceUtils.profileResponse(username, loggedUserId)));
    }

    @DeleteMapping(value = "/{username}/follow", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> unfollow(@PathVariable String username) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        unfollowUserByUsername.handle(loggedUserId, username);
        return ResponseEntity.ok(objectMapper.writeValueAsString(
                resourceUtils.profileResponse(username, loggedUserId)));
    }
}
