package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.realworldapi.application.web.model.request.LoginRequestWrapper;
import org.example.realworldapi.application.web.model.request.NewUserRequestWrapper;
import org.example.realworldapi.application.web.model.response.UserResponse;
import org.example.realworldapi.domain.exception.InvalidPasswordException;
import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.feature.CreateUser;
import org.example.realworldapi.domain.feature.LoginUser;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.infrastructure.config.SerializerConfig;
import org.example.realworldapi.infrastructure.web.exception.UnauthorizedException;
import org.example.realworldapi.infrastructure.web.provider.TokenProvider;

@Path("/users")
public class UsersResource {

    @Inject
    CreateUser createUser;
    @Inject
    LoginUser loginUser;
    @Inject
    TokenProvider tokenProvider;
    @Inject
    SerializerConfig serializerConfig;

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
            @Valid
            NewUserRequestWrapper newUserRequest) throws JsonProcessingException {
        final var objectMapper = serializerConfig.getWrappingObjectMapper();
        final var user = createUser.handle(newUserRequest.getUser().toCreateUserInput());
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return Response.ok(objectMapper.writeValueAsString(new UserResponse(user, token))).status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
            @Valid
            LoginRequestWrapper loginRequest) throws JsonProcessingException {
        final var objectMapper = serializerConfig.getWrappingObjectMapper();
        User user;

        try {
            user = loginUser.handle(loginRequest.getUser().toLoginUserInput());
        } catch (UserNotFoundException | InvalidPasswordException ex) {
            throw new UnauthorizedException();
        }
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return Response.ok(objectMapper.writeValueAsString(new UserResponse(user, token))).status(Response.Status.OK).build();
    }
}
