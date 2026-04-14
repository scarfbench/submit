package spring.examples.tutorial.helloservice;

import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class Application extends jakarta.ws.rs.core.Application {
	// Jakarta EE REST application configuration
	// No explicit configuration needed - CDI will discover resources automatically
}
