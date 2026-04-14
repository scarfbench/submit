package spring.tutorial.customer;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/webapi")
public class CustomerApplication extends Application {
    // JAX-RS will automatically discover and register all @Path annotated classes
}
