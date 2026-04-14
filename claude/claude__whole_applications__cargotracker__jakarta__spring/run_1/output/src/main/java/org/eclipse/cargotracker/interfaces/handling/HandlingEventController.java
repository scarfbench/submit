package org.eclipse.cargotracker.interfaces.handling;

import java.time.LocalDateTime;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateConverter;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for handling event registration.
 * Replaces the EventLogger JSF backing bean with REST API endpoints.
 */
@RestController
@RequestMapping("/api/handling")
public class HandlingEventController {

    @Autowired
    private ApplicationEvents applicationEvents;

    @PostMapping("/register")
    public ResponseEntity<Void> registerHandlingEvent(
            @RequestParam String completionTime,
            @RequestParam String trackingId,
            @RequestParam(required = false) String voyageNumber,
            @RequestParam String unLocode,
            @RequestParam String eventType) {

        LocalDateTime completionDateTime = DateConverter.toDateTime(completionTime);
        TrackingId tid = new TrackingId(trackingId);
        VoyageNumber vn = (voyageNumber != null && !voyageNumber.isEmpty()) ? new VoyageNumber(voyageNumber) : null;
        UnLocode loc = new UnLocode(unLocode);
        HandlingEvent.Type type = HandlingEvent.Type.valueOf(eventType);

        HandlingEventRegistrationAttempt attempt = new HandlingEventRegistrationAttempt(
            LocalDateTime.now(), completionDateTime, tid, vn, type, loc);

        applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);

        return ResponseEntity.ok().build();
    }
}
