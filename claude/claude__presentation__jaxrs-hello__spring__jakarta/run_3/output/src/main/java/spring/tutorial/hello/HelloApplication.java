package spring.tutorial.hello;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class HelloApplication extends Application {
    // JAX-RS will automatically discover and register all resource classes
    // No additional configuration needed
}
