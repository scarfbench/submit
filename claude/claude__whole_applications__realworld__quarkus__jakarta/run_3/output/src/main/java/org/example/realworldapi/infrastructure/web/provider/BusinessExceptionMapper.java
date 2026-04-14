package org.example.realworldapi.infrastructure.web.provider;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.realworldapi.domain.exception.*;

import java.util.Map;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof UserNotFoundException
                || exception instanceof ArticleNotFoundException
                || exception instanceof CommentNotFoundException
                || exception instanceof TagNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof EmailAlreadyExistsException
                || exception instanceof UsernameAlreadyExistsException) {
            return Response.status(422)
                    .entity(errorBody(exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof InvalidPasswordException) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorBody("invalid credentials"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof ModelValidationException) {
            return Response.status(422)
                    .entity(errorBody(exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof jakarta.ws.rs.NotAuthorizedException
                || exception instanceof SecurityException) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorBody("unauthorized"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof jakarta.ws.rs.WebApplicationException wae) {
            return wae.getResponse();
        }

        // Log unexpected errors
        exception.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorBody("internal server error"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private Map<String, Object> errorBody(String message) {
        return Map.of("errors", Map.of("body", new String[]{message}));
    }
}
