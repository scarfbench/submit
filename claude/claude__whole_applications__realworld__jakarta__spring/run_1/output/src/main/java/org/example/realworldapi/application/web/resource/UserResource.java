package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.example.realworldapi.application.web.model.request.UpdateUserRequestWrapper;
import org.example.realworldapi.application.web.model.response.UserResponse;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.UpdateUser;
import org.example.realworldapi.infrastructure.web.provider.TokenProvider;
import org.example.realworldapi.infrastructure.web.security.annotation.Authenticated;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserResource {

    private final FindUserById findUserById;
    private final UpdateUser updateUser;
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    public UserResource(
            FindUserById findUserById,
            UpdateUser updateUser,
            TokenProvider tokenProvider,
            @Qualifier("wrapRootValueObjectMapper") ObjectMapper objectMapper) {
        this.findUserById = findUserById;
        this.updateUser = updateUser;
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Authenticated
    public ResponseEntity<String> getUser(@RequestAttribute("loggedUserId") UUID loggedUserId) throws JsonProcessingException {
        final var user = findUserById.handle(loggedUserId);
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return ResponseEntity.ok(objectMapper.writeValueAsString(new UserResponse(user, token)));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Authenticated
    public ResponseEntity<String> update(
            @RequestAttribute("loggedUserId") UUID loggedUserId,
            @Valid @RequestBody UpdateUserRequestWrapper updateUserRequest) throws JsonProcessingException {
        final var user = updateUser.handle(updateUserRequest.getUser().toUpdateUserInput(loggedUserId));
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return ResponseEntity.ok(objectMapper.writeValueAsString(new UserResponse(user, token)));
    }
}
