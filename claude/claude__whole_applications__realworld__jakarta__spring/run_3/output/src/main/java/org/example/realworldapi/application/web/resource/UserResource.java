package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.realworldapi.application.web.model.request.UpdateUserRequestWrapper;
import org.example.realworldapi.application.web.model.response.UserResponse;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.UpdateUser;
import org.example.realworldapi.infrastructure.web.provider.TokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserResource {

    private final FindUserById findUserById;
    private final UpdateUser updateUser;
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final ResourceUtils resourceUtils;

    public UserResource(
            FindUserById findUserById,
            UpdateUser updateUser,
            TokenProvider tokenProvider,
            @Qualifier("wrappingObjectMapper") ObjectMapper objectMapper,
            ResourceUtils resourceUtils) {
        this.findUserById = findUserById;
        this.updateUser = updateUser;
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
        this.resourceUtils = resourceUtils;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getUser() throws JsonProcessingException {
        final var userId = resourceUtils.getLoggedUserId();
        final var user = findUserById.handle(userId);
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return ResponseEntity.ok(objectMapper.writeValueAsString(new UserResponse(user, token)));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> update(
            @Valid @RequestBody UpdateUserRequestWrapper updateUserRequest) throws JsonProcessingException {
        final var userId = resourceUtils.getLoggedUserId();
        final var user = updateUser.handle(updateUserRequest.getUser().toUpdateUserInput(userId));
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return ResponseEntity.ok(objectMapper.writeValueAsString(new UserResponse(user, token)));
    }
}
