package io.quarkuscoffeeshop.coffeeshop.web;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application class for the Coffee Shop.
 * Maps all REST endpoints under the root path.
 */
@ApplicationPath("/")
public class CoffeeshopApplication extends Application {
}
