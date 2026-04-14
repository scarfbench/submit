package springboot.tutorial.async;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Application configuration for Jakarta EE.
 * No main class needed - deployed to Jakarta EE server.
 */
@ApplicationScoped
public class AsyncApplication {
    // CDI will automatically discover and manage beans
}
