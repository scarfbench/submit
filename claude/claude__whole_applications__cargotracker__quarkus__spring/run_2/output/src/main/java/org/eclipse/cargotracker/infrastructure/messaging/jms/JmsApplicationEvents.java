package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class JmsApplicationEvents implements ApplicationEvents, Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${app.jms.CargoHandledQueue}")
    private String cargoHandledQueue;

    @Value("${app.jms.MisdirectedCargoQueue}")
    private String misdirectedCargoQueue;

    @Value("${app.jms.DeliveredCargoQueue}")
    private String deliveredCargoQueue;

    @Value("${app.jms.HandlingEventRegistrationAttemptQueue}")
    private String handlingEventQueue;

    @Autowired
    private Logger logger;

    @Override
    public void cargoWasHandled(HandlingEvent event) {
        Cargo cargo = event.getCargo();
        logger.log(Level.INFO, "Cargo was handled {0}", cargo);
        jmsTemplate.convertAndSend(cargoHandledQueue, cargo.getTrackingId().getIdString());
    }

    public void cargoWasRerouted(Cargo cargo) {
        logger.log(Level.INFO, "Cargo was rerouted {0}", cargo);
    }

    @Override
    public void cargoWasMisdirected(Cargo cargo) {
        logger.log(Level.INFO, "Cargo was misdirected {0}", cargo);
        jmsTemplate.convertAndSend(misdirectedCargoQueue, cargo.getTrackingId().getIdString());
    }

    @Override
    public void cargoHasArrived(Cargo cargo) {
        logger.log(Level.INFO, "Cargo has arrived {0}", cargo);
        jmsTemplate.convertAndSend(deliveredCargoQueue, cargo.getTrackingId().getIdString());
    }

    @Override
    public void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt) {
        logger.log(Level.INFO, "Received handling event registration attempt {0}", attempt);
        jmsTemplate.convertAndSend(handlingEventQueue, attempt);
    }
}
