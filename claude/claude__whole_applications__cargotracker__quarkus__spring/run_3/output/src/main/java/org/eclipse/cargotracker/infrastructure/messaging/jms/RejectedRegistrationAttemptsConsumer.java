package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;

/**
 * This consumer would handle rejected registration attempts. In the Spring Event
 * based architecture, rejected events are not explicitly published, so this is
 * a placeholder. The original Quarkus version listened on a JMS queue.
 */
@Component
public class RejectedRegistrationAttemptsConsumer {

    private static final Logger logger = Logger.getLogger(RejectedRegistrationAttemptsConsumer.class.getName());

    // No event listener needed since rejected registration attempts
    // are not currently published as Spring events.
    // If needed, create a RejectedRegistrationEvent class and publish it.
}
