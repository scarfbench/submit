package quarkus.tutorial.mood;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application configuration
 */
@ApplicationPath("/")
public class RestApplication extends Application {
    // No additional configuration needed - will autodiscover resources
}
