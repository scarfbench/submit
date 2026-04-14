package spring.tutorial.mood.web;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class MoodRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Set mood as a header so it can be accessed by the controller
        requestContext.getHeaders().putSingle("X-Mood", "awake");
    }
}
