package spring.examples.tutorial.converter;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Jakarta EE REST Application Configuration
 * Defines the base path for all REST endpoints
 */
@ApplicationPath("/")
public class ConverterApplication extends Application {
	// JAX-RS will automatically discover and register all @Path annotated classes
}
