package quarkus.examples.tutorial;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/converter")
public class ConverterApplication extends Application {
    // This class activates JAX-RS and sets the base path
}
