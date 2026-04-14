package org.eclipse.cargotracker.infrastructure.messaging.jms;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** Consumes handling event registration attempt messages and delegates to proper registration. */
@Singleton
@Startup
public class HandlingEventRegistrationAttemptConsumer implements Runnable {

  @Inject
  private HandlingEventService handlingEventService;

  @Inject
  private ConnectionFactory connectionFactory;

  @Inject
  @ConfigProperty(name = "app.jms.HandlingEventRegistrationAttemptQueue",
      defaultValue = "HandlingEventRegistrationAttemptQueue")
  private String handlingEventRegistrationAttemptQueue;

  private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

  @PostConstruct
  public void onStart() {
    scheduler.submit(this);
  }

  @PreDestroy
  public void onStop() {
    scheduler.shutdown();
  }

  @Override
  public void run() {
    try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
      JMSConsumer consumer =
          context.createConsumer(context.createQueue(handlingEventRegistrationAttemptQueue));
      while (true) {
        Message message = consumer.receive();
        ObjectMessage objectMessage = (ObjectMessage) message;
        HandlingEventRegistrationAttempt attempt =
            (HandlingEventRegistrationAttempt) objectMessage.getObject();
        handlingEventService.registerHandlingEvent(
            attempt.getCompletionTime(),
            attempt.getTrackingId(),
            attempt.getVoyageNumber(),
            attempt.getUnLocode(),
            attempt.getType());
      }
    } catch (JMSException | CannotCreateHandlingEventException e) {
      // Poison messages will be placed on dead-letter queue.
      throw new RuntimeException("Error occurred processing message", e);
    }
  }

}
