/**
 * JAX-RS Application class for DayTrader.
 * Configures the JAX-RS application with the /rest base path.
 */
package com.ibm.websphere.samples.daytrader.jaxrs;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/rest")
public class JaxrsApplication extends Application {
}
