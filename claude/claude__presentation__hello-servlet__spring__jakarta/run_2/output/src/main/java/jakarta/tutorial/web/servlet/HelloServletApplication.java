package jakarta.tutorial.web.servlet;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class HelloServletApplication extends Application {
    // Jakarta EE will automatically discover and register JAX-RS resources
}