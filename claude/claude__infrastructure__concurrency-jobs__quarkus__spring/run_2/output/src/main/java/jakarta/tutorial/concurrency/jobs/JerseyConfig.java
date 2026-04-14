package jakarta.tutorial.concurrency.jobs;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        // Register the JAX-RS application
        register(RestApplication.class);
        // Scan for JAX-RS resources in the packages
        packages("jakarta.tutorial.concurrency.jobs");
    }
}
