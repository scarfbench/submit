package jakarta.examples.tutorial;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/converter")
public class ConverterApplication extends Application {
    // This class is intentionally left empty
    // JAX-RS will automatically discover and register all resource classes
}
