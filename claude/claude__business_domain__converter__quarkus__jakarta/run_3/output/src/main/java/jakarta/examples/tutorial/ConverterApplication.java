package jakarta.examples.tutorial;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class ConverterApplication extends Application {
    // JAX-RS will automatically discover all resource classes
}
