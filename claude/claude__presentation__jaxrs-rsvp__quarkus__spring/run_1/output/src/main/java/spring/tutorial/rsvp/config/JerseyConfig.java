package spring.tutorial.rsvp.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import spring.tutorial.rsvp.ejb.ResponseBean;
import spring.tutorial.rsvp.ejb.StatusBean;

/**
 * Jersey Configuration for JAX-RS endpoints
 */
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        // Register JAX-RS resource classes
        register(ResponseBean.class);
        register(StatusBean.class);
    }
}
