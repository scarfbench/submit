package jakarta.tutorial.rsvp.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class JaxrsApplication extends Application {
    // JAX-RS application configuration
    // The @ApplicationPath annotation defines the base URI for all resource URIs
}
