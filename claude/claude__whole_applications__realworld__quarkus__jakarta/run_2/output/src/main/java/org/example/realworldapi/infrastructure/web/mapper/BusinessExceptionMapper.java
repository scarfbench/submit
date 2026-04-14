package org.example.realworldapi.infrastructure.web.mapper;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.realworldapi.application.web.model.response.ErrorResponse;
import org.example.realworldapi.domain.exception.*;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(Exception e) {

    if (e instanceof NotAuthorizedException) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(new ErrorResponse("Unauthorized"))
          .build();
    }

    if (e instanceof ForbiddenException) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(new ErrorResponse("Forbidden"))
          .build();
    }

    if (e instanceof EmailAlreadyExistsException) {
      return Response.status(Response.Status.CONFLICT)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    }

    if (e instanceof UsernameAlreadyExistsException) {
      return Response.status(Response.Status.CONFLICT)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    }

    if (e instanceof UserNotFoundException) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    }

    if (e instanceof InvalidPasswordException) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    }

    if (e instanceof TagNotFoundException) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    }

    if (e instanceof ArticleNotFoundException) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    }

    if (e instanceof CommentNotFoundException) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    }

    if (e instanceof ModelValidationException) {
      return Response.status(422).entity(new ErrorResponse(e.getMessage())).build();
    }

    // Default - internal server error
    System.err.println("Unhandled exception: " + e.getClass().getName() + ": " + e.getMessage());
    e.printStackTrace();
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(new ErrorResponse("Internal Server Error"))
        .build();
  }
}
