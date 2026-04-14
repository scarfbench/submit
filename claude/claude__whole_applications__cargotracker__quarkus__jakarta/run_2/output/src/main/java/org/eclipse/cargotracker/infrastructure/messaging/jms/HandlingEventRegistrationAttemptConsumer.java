package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/HandlingEventRegistrationAttemptQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class HandlingEventRegistrationAttemptConsumer implements MessageListener {

  @Inject
  private Logger logger;

  @Inject
  private HandlingEventService handlingEventService;

  @Override
  public void onMessage(Message message) {
    try {
      ObjectMessage objectMessage = (ObjectMessage) message;
      HandlingEventRegistrationAttempt attempt =
          (HandlingEventRegistrationAttempt) objectMessage.getObject();
      handlingEventService.registerHandlingEvent(
          attempt.getCompletionTime(),
          attempt.getTrackingId(),
          attempt.getVoyageNumber(),
          attempt.getUnLocode(),
          attempt.getType());
    } catch (JMSException | CannotCreateHandlingEventException e) {
      logger.log(Level.SEVERE, "Error processing JMS message", e);
      throw new RuntimeException("Error occurred processing message", e);
    }
  }
}
