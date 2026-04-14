package com.ibm.websphere.samples.daytrader.jaxrs;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application class.
 * Replaces quarkus.rest.path=/rest in application.properties.
 */
@ApplicationPath("/rest")
public class TradeApplication extends Application {
}
