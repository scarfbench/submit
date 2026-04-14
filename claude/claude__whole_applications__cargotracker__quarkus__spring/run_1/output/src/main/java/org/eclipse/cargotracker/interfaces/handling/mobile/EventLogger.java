package org.eclipse.cargotracker.interfaces.handling.mobile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.cargo.TransportStatus;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.infrastructure.persistence.jpa.JpaCargoRepository;
import org.eclipse.cargotracker.infrastructure.persistence.jpa.JpaLocationRepository;
import org.eclipse.cargotracker.infrastructure.persistence.jpa.JpaVoyageRepository;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller replacing the JSF-based mobile event logger.
 * Provides endpoints for logging handling events.
 */
@RestController
@RequestMapping("/rest/event-logger")
public class EventLogger {

    @Autowired
    private JpaCargoRepository cargoRepository;

    @Autowired
    private JpaLocationRepository locationRepository;

    @Autowired
    private JpaVoyageRepository voyageRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @GetMapping("/tracking-ids")
    public List<String> getTrackingIds() {
        return cargoRepository.listAll().stream()
                .filter(cargo -> !cargo.getItinerary().getLegs().isEmpty()
                        && !cargo.getDelivery().getTransportStatus()
                                .sameValueAs(TransportStatus.CLAIMED))
                .map(cargo -> cargo.getTrackingId().getIdString())
                .collect(Collectors.toList());
    }

    @GetMapping("/locations")
    public List<Map<String, String>> getLocations() {
        return locationRepository.listAll().stream()
                .map(location -> Map.of(
                        "code", location.getUnLocode().getIdString(),
                        "name", location.getName() + " (" + location.getUnLocode().getIdString() + ")"))
                .collect(Collectors.toList());
    }

    @GetMapping("/voyages")
    public List<String> getVoyages() {
        return voyageRepository.listAll().stream()
                .map(voyage -> voyage.getVoyageNumber().getIdString())
                .collect(Collectors.toList());
    }

    @GetMapping("/event-types")
    public HandlingEvent.Type[] getEventTypes() {
        return HandlingEvent.Type.values();
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submit(
            @RequestParam String trackingId,
            @RequestParam String location,
            @RequestParam HandlingEvent.Type eventType,
            @RequestParam(required = false) String voyageNumber,
            @RequestParam String completionTime) {

        if (eventType.requiresVoyage() && (voyageNumber == null || voyageNumber.isEmpty())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Voyage number is required for LOAD/UNLOAD events"));
        }

        TrackingId tid = new TrackingId(trackingId);
        UnLocode loc = new UnLocode(location);
        VoyageNumber voyage = (voyageNumber != null && !voyageNumber.isEmpty())
                ? new VoyageNumber(voyageNumber) : null;
        LocalDateTime completion = LocalDateTime.parse(completionTime);

        HandlingEventRegistrationAttempt attempt =
                new HandlingEventRegistrationAttempt(
                        LocalDateTime.now(), completion, tid, voyage, eventType, loc);

        applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);

        return ResponseEntity.ok(Map.of("message", "Event submitted"));
    }
}
