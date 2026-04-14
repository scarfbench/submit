package jakarta.tutorial.rsvp;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/webapi")
public class RsvpApplication extends Application {
    // JAX-RS will automatically discover and register REST resources
}
