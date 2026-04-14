package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Qualifier;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.FollowUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.infrastructure.web.security.annotation.Authenticated;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profiles")
public class ProfilesResource {

    private final FollowUserByUsername followUserByUsername;
    private final UnfollowUserByUsername unfollowUserByUsername;
    private final ResourceUtils resourceUtils;
    private final ObjectMapper objectMapper;

    public ProfilesResource(
            FollowUserByUsername followUserByUsername,
            UnfollowUserByUsername unfollowUserByUsername,
            ResourceUtils resourceUtils,
            @Qualifier("wrapRootValueObjectMapper") ObjectMapper objectMapper) {
        this.followUserByUsername = followUserByUsername;
        this.unfollowUserByUsername = unfollowUserByUsername;
        this.resourceUtils = resourceUtils;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Authenticated(optional = true)
    public ResponseEntity<String> getProfile(
            @PathVariable @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK) String username,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) throws JsonProcessingException {
        final var profileResponse = resourceUtils.profileResponse(username, loggedUserId);
        return ResponseEntity.ok(objectMapper.writeValueAsString(profileResponse));
    }

    @PostMapping(value = "/{username}/follow", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Authenticated
    public ResponseEntity<String> follow(
            @PathVariable @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK) String username,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) throws JsonProcessingException {
        followUserByUsername.handle(loggedUserId, username);
        return ResponseEntity.ok(objectMapper.writeValueAsString(resourceUtils.profileResponse(username, loggedUserId)));
    }

    @DeleteMapping(value = "/{username}/follow", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Authenticated
    public ResponseEntity<String> unfollow(
            @PathVariable @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK) String username,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) throws JsonProcessingException {
        unfollowUserByUsername.handle(loggedUserId, username);
        return ResponseEntity.ok(objectMapper.writeValueAsString(resourceUtils.profileResponse(username, loggedUserId)));
    }
}
