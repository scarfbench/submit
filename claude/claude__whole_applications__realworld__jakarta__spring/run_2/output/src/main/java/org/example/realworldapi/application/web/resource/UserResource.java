package org.example.realworldapi.application.web.resource;

import org.example.realworldapi.application.web.model.request.UpdateUserRequestWrapper;
import org.example.realworldapi.application.web.model.response.UserResponse;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.UpdateUser;
import org.example.realworldapi.infrastructure.web.provider.TokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserResource {

    @Autowired
    private FindUserById findUserById;
    @Autowired
    private UpdateUser updateUser;
    @Autowired
    private TokenProvider tokenProvider;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getUser(@RequestAttribute UUID loggedUserId) {
        final var user = findUserById.handle(loggedUserId);
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return ResponseEntity.ok(Collections.singletonMap("user", new UserResponse(user, token)));
    }

    @PutMapping(consumes = "application/json", produces = "application/json")
    @Transactional
    public ResponseEntity<?> update(
            @RequestAttribute UUID loggedUserId,
            @Valid @RequestBody UpdateUserRequestWrapper updateUserRequest) {
        final var user = updateUser.handle(updateUserRequest.getUser().toUpdateUserInput(loggedUserId));
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return ResponseEntity.ok(Collections.singletonMap("user", new UserResponse(user, token)));
    }
}
