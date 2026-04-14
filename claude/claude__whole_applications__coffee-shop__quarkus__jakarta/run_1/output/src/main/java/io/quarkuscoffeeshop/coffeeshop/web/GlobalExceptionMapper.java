package io.quarkuscoffeeshop.coffeeshop.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Global exception mapper to handle uncaught exceptions gracefully.
 * Prevents Liberty from putting multi-line error messages into HTTP headers
 * which causes "Invalid LF not followed by whitespace" errors.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOGGER.error("Unhandled exception in REST resource", exception);

        // Sanitize error message - remove newlines that cause HTTP header issues
        String message = exception.getMessage();
        if (message != null) {
            message = message.replace('\n', ' ').replace('\r', ' ');
        } else {
            message = "Internal server error";
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + message.replace("\"", "'") + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
