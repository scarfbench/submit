package org.quarkus.samples.petclinic;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Jakarta EE JAX-RS Application class.
 * Maps all JAX-RS resources under the root path.
 */
@ApplicationPath("/")
public class JaxRsApplication extends Application {
}
