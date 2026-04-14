package com.example.orderspring;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Jakarta EE application marker class.
 * The application is deployed as a WAR to a Jakarta EE container.
 * No main method is needed - the container manages lifecycle.
 */
@ApplicationScoped
public class OrderApplication {
    // Jakarta EE container manages the application lifecycle
}
