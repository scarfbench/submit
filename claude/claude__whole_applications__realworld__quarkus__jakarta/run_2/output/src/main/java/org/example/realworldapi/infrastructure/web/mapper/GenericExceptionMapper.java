package org.example.realworldapi.infrastructure.web.mapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.realworldapi.application.web.model.response.ErrorResponse;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<WebApplicationException> {

  @Override
  public Response toResponse(WebApplicationException e) {
    int status = e.getResponse() != null ? e.getResponse().getStatus() : 500;

    if (status == 401) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(new ErrorResponse("Unauthorized"))
          .build();
    }

    if (status == 403) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(new ErrorResponse("Forbidden"))
          .build();
    }

    return Response.status(status).entity(new ErrorResponse(e.getMessage())).build();
  }
}
