package org.eclipse.cargotracker.config;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * JMS resource configuration is no longer needed as the application uses CDI events.
 * This class is kept for backwards compatibility but may be removed in the future.
 */
@ApplicationScoped
public class JmsResourceConfiguration {
}
