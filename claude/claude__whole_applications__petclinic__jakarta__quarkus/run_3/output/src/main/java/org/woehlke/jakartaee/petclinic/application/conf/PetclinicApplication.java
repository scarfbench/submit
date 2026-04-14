package org.woehlke.jakartaee.petclinic.application.conf;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Quarkus JAX-RS Application configuration
 */
@ApplicationPath("/rest")
public class PetclinicApplication extends Application {

}
