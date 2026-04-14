package org.example.realworldapi.application.web.resource;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.realworldapi.application.web.model.request.LoginRequest;
import org.example.realworldapi.application.web.model.request.NewUserRequest;
import org.example.realworldapi.application.web.model.response.UserResponse;
import org.example.realworldapi.domain.exception.InvalidPasswordException;
import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.feature.CreateUser;
import org.example.realworldapi.domain.feature.LoginUser;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.infrastructure.provider.JwtTokenProvider;

@Dependent
@Path("/users")
public class UsersResource {

  @Inject private CreateUser createUser;
  @Inject private LoginUser loginUser;
  @Inject private JwtTokenProvider tokenProvider;

  @POST
  @Transactional
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response create(
      @Valid @NotNull(message = ValidationMessages.REQUEST_BODY_MUST_BE_NOT_NULL)
          NewUserRequest newUserRequest) {
    final var user = createUser.handle(newUserRequest.toCreateUserInput());
    final var token = tokenProvider.createUserToken(user.getId().toString());
    return Response.ok(new UserResponse(user, token)).status(Response.Status.CREATED).build();
  }

  @POST
  @Transactional
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response login(
      @Valid @NotNull(message = ValidationMessages.REQUEST_BODY_MUST_BE_NOT_NULL)
          LoginRequest loginRequest) {
    User user;
    try {
      user = loginUser.handle(loginRequest.toLoginUserInput());
    } catch (UserNotFoundException | InvalidPasswordException ex) {
      throw new NotAuthorizedException("Unauthorized");
    }
    final var token = tokenProvider.createUserToken(user.getId().toString());
    return Response.ok(new UserResponse(user, token)).status(Response.Status.OK).build();
  }
}
