package spring.tutorial.mood.web;

import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class TimeOfDayFilter implements ContainerRequestFilter {

    // Provide a default; can be overridden if desired
    private String mood = "awake";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Set the mood as a header so it can be accessed by the controller
        requestContext.getHeaders().putSingle("X-Mood", mood);
    }
}
