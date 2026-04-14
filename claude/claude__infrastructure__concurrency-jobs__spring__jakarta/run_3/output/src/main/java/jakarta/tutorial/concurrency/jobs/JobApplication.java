package jakarta.tutorial.concurrency.jobs;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/webapi")
public class JobApplication extends Application {
    // JAX-RS will automatically discover and register all resource classes
    // annotated with @Path in the classpath
}