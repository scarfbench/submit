package spring.examples.tutorial.counter;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class CounterApplication extends Application {
    // This class serves as the Jakarta REST (JAX-RS) application configuration
    // No explicit configuration needed - CDI will automatically discover resources
}
