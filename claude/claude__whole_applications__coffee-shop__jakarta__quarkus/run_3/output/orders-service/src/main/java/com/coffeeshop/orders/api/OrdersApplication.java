package com.coffeeshop.orders.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

// Single JAX-RS Application class for Quarkus - sets the base path for all REST resources
@ApplicationPath("/api")
public class OrdersApplication extends Application {}
