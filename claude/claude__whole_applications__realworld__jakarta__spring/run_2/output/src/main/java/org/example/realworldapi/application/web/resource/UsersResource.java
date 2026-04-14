package org.example.realworldapi.application.web.resource;

import org.example.realworldapi.application.web.model.request.LoginRequestWrapper;
import org.example.realworldapi.application.web.model.request.NewUserRequestWrapper;
import org.example.realworldapi.application.web.model.response.UserResponse;
import org.example.realworldapi.domain.exception.InvalidPasswordException;
import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.feature.CreateUser;
import org.example.realworldapi.domain.feature.LoginUser;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.infrastructure.web.exception.UnauthorizedException;
import org.example.realworldapi.infrastructure.web.provider.TokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/users")
public class UsersResource {

    @Autowired
    private CreateUser createUser;
    @Autowired
    private LoginUser loginUser;
    @Autowired
    private TokenProvider tokenProvider;

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Transactional
    public ResponseEntity<?> create(@Valid @RequestBody NewUserRequestWrapper newUserRequest) {
        final var user = createUser.handle(newUserRequest.getUser().toCreateUserInput());
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("user", new UserResponse(user, token)));
    }

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestWrapper loginRequest) {
        User user;
        try {
            user = loginUser.handle(loginRequest.getUser().toLoginUserInput());
        } catch (UserNotFoundException | InvalidPasswordException ex) {
            throw new UnauthorizedException();
        }
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return ResponseEntity.ok(Collections.singletonMap("user", new UserResponse(user, token)));
    }
}
