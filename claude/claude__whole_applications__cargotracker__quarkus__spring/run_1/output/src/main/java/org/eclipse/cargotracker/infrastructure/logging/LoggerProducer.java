package org.eclipse.cargotracker.infrastructure.logging;

import org.springframework.stereotype.Component;

/**
 * In Spring Boot, Logger instances are typically created directly in each class
 * using Logger.getLogger(ClassName.class.getName()).
 * This class is kept as a placeholder for compatibility.
 */
@Component
public class LoggerProducer {
    // No-op: Spring does not use CDI producer for loggers.
    // Each class should create its own Logger instance.
}
