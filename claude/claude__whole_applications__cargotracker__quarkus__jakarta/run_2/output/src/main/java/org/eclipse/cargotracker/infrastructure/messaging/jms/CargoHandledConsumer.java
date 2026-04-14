package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/CargoHandledQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class CargoHandledConsumer implements MessageListener {

  @Inject
  private Logger logger;

  @Inject
  private CargoInspectionService cargoInspectionService;

  @Override
  public void onMessage(Message message) {
    try {
      TextMessage textMessage = (TextMessage) message;
      String trackingIdString = textMessage.getText();
      cargoInspectionService.inspectCargo(new TrackingId(trackingIdString));
    } catch (JMSException e) {
      logger.log(Level.SEVERE, "Error processing JMS message", e);
    }
  }
}
