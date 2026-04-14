package quarkus.tutorial.web.dukeetf;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class RestApplication extends Application {
    // JAX-RS will automatically discover and register all @Path annotated classes
}
