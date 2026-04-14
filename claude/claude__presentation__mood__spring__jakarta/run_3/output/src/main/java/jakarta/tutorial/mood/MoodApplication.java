package jakarta.tutorial.mood;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class MoodApplication extends Application {
    // JAX-RS application entry point
    // This class automatically discovers and registers all JAX-RS resources
}
