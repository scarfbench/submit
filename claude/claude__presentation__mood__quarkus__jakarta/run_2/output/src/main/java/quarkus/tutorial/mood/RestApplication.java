package quarkus.tutorial.mood;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application configuration.
 */
@ApplicationPath("/")
public class RestApplication extends Application {
    // Default configuration, all resources and providers will be automatically discovered
}
