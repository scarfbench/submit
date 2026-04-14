package jakarta.tutorial.taskcreator;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/taskcreator")
public class TaskCreatorApplication extends Application {
    // JAX-RS Application class for Jakarta EE
    // No additional configuration needed - CDI will discover all REST endpoints
}