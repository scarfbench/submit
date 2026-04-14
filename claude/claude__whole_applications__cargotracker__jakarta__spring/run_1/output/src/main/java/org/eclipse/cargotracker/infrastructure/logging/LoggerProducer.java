package org.eclipse.cargotracker.infrastructure.logging;

import org.springframework.context.annotation.Configuration;

/**
 * In Spring Boot, logging is configured automatically.
 * Each class should use: private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
 * or use @Slf4j annotation from Lombok.
 */
@Configuration
public class LoggerProducer {
  // No CDI @Produces needed in Spring
  // Logging should be obtained directly via LoggerFactory.getLogger() in each class
}
