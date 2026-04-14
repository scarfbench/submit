package jakarta.tutorial.addressbook.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application activator.
 * Configures REST API under /api path.
 */
@ApplicationPath("/api")
public class JaxRsActivator extends Application {
}
