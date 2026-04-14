package jakarta.tutorial.taskcreator;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application configuration for REST endpoints.
 */
@ApplicationPath("/taskcreator")
public class RestApplication extends Application {
    // This will scan and register all JAX-RS resources in the same package
}
