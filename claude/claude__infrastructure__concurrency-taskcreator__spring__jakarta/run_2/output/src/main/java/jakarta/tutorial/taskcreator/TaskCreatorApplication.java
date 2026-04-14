package jakarta.tutorial.taskcreator;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/taskcreator")
public class TaskCreatorApplication extends Application {
    // JAX-RS will auto-discover all @Path annotated classes
}