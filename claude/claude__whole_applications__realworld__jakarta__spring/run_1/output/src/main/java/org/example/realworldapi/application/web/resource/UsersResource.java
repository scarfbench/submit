package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UsersResource {

    private final CreateUser createUser;
    private final LoginUser loginUser;
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    public UsersResource(
            CreateUser createUser,
            LoginUser loginUser,
            TokenProvider tokenProvider,
            @Qualifier("wrapRootValueObjectMapper") ObjectMapper objectMapper) {
        this.createUser = createUser;
        this.loginUser = loginUser;
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> create(@Valid @RequestBody NewUserRequestWrapper newUserRequest) throws JsonProcessingException {
        final var user = createUser.handle(newUserRequest.getUser().toCreateUserInput());
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(objectMapper.writeValueAsString(new UserResponse(user, token)));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestWrapper loginRequest) throws JsonProcessingException {
        User user;

        try {
            user = loginUser.handle(loginRequest.getUser().toLoginUserInput());
        } catch (UserNotFoundException | InvalidPasswordException ex) {
            throw new UnauthorizedException();
        }
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return ResponseEntity.ok(objectMapper.writeValueAsString(new UserResponse(user, token)));
    }
}
