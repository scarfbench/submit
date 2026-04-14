package quarkus.tutorial.web.servlet;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class RestApplication extends Application {
    // This class is used to configure the JAX-RS application
    // The @ApplicationPath annotation defines the base URI for all resource URIs
}
