package jakarta.examples.tutorial.counter;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Jakarta EE Application Configuration
 * This class configures the JAX-RS application
 */
@ApplicationPath("/api")
public class CounterApplication extends Application {
    // No additional configuration needed for basic setup
    // The @ApplicationPath annotation defines the base URI for all REST endpoints
}
