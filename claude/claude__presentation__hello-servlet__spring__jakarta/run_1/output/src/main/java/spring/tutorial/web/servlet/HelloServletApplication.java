package spring.tutorial.web.servlet;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class HelloServletApplication extends Application {
    // JAX-RS will automatically discover and register all @Path annotated classes
}