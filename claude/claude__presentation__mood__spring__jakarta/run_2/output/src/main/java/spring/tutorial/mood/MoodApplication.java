package spring.tutorial.mood;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class MoodApplication extends Application {
    // Jakarta REST will automatically discover and register all JAX-RS resources
}
