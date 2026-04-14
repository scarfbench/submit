package spring.tutorial.mood.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class TimeOfDayFilter implements ContainerRequestFilter {

    @Inject
    MoodService moodService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Example: set mood based on time/mood (same behavior you had)
        moodService.setMood("awake");
        // ... any time-of-day logic you previously had ...
    }
}
