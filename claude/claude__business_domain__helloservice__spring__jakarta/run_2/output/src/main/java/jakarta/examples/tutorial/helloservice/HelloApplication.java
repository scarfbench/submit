package jakarta.examples.tutorial.helloservice;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class HelloApplication extends Application {
	// No additional configuration needed
	// JAX-RS will automatically discover and register resources
}
