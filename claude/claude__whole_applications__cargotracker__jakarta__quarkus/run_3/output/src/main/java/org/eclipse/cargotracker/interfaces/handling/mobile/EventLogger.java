package org.eclipse.cargotracker.interfaces.handling.mobile;

import java.time.LocalDateTime;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

/**
 * Event logger for mobile handling events. Converted from JSF backing bean to CDI bean.
 * Functionality exposed via REST endpoints.
 */
@ApplicationScoped
public class EventLogger {
  @Inject private ApplicationEvents applicationEvents;

  public void submitEvent(
      String trackingId,
      String location,
      HandlingEvent.Type eventType,
      String voyageNumber,
      LocalDateTime completionTime) {

    VoyageNumber voyage = null;
    if (voyageNumber != null && !voyageNumber.isEmpty()) {
      voyage = new VoyageNumber(voyageNumber);
    }

    HandlingEventRegistrationAttempt attempt =
        new HandlingEventRegistrationAttempt(
            LocalDateTime.now(),
            completionTime,
            new TrackingId(trackingId),
            voyage,
            eventType,
            new UnLocode(location));

    applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
  }
}
