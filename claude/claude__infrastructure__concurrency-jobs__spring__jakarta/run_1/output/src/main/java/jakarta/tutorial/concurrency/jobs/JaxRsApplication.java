package jakarta.tutorial.concurrency.jobs;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/webapi")
public class JaxRsApplication extends Application {
    // JAX-RS will automatically discover and register all @Path annotated classes
}