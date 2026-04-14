package org.eclipse.cargotracker.application.internal;

import java.time.LocalDateTime;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventFactory;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = CannotCreateHandlingEventException.class)
public class DefaultHandlingEventService implements HandlingEventService {

    @Autowired
    private ApplicationEvents applicationEvents;
    @Autowired
    private HandlingEventRepository handlingEventRepository;
    @Autowired
    private HandlingEventFactory handlingEventFactory;
    @Autowired
    private Logger logger;

    @Override
    public void registerHandlingEvent(
            LocalDateTime completionTime,
            TrackingId trackingId,
            VoyageNumber voyageNumber,
            UnLocode unLocode,
            HandlingEvent.Type type)
            throws CannotCreateHandlingEventException {
        LocalDateTime registrationTime = LocalDateTime.now();

        HandlingEvent event =
            handlingEventFactory.createHandlingEvent(
                registrationTime, completionTime, trackingId, voyageNumber, unLocode, type);

        handlingEventRepository.store(event);
        applicationEvents.cargoWasHandled(event);

        logger.info("Registered handling event");
    }
}
