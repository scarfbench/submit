package org.eclipse.cargotracker.interfaces.handling.mobile;

import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateConverter;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.cargo.TransportStatus;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Handles mobile event logging. Migrated from JSF/PrimeFaces to Spring Boot.
 * JSF-specific UI interactions (FacesContext, SelectItem, FlowEvent) have been removed.
 */
@Component("eventLogger")
@Scope("prototype")
public class EventLogger implements Serializable {

  private static final long serialVersionUID = 1L;

  @Autowired private CargoRepository cargoRepository;
  @Autowired private LocationRepository locationRepository;
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private ApplicationEvents applicationEvents;

  private List<String> trackingIds;
  private List<String> locations;
  private List<String> voyages;

  private String trackingId;
  private String location;
  private HandlingEvent.Type eventType;
  private String voyageNumber;
  private LocalDateTime completionTime;

  public String getTrackingId() {
    return trackingId;
  }

  public void setTrackingId(String trackingId) {
    this.trackingId = trackingId;
  }

  public List<String> getTrackingIds() {
    return trackingIds;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public List<String> getLocations() {
    return locations;
  }

  // Move this to a separate utility if it is used in other parts of the UI.
  public Map<HandlingEvent.Type, HandlingEvent.Type> getEventTypes() {
    return Collections.unmodifiableMap(
        Arrays.asList(HandlingEvent.Type.values())
            .stream()
            .collect(toMap(Function.identity(), Function.identity())));
  }

  public HandlingEvent.Type getEventType() {
    return eventType;
  }

  public void setEventType(HandlingEvent.Type eventType) {
    this.eventType = eventType;
  }

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public void setVoyageNumber(String voyageNumber) {
    this.voyageNumber = voyageNumber;
  }

  public List<String> getVoyages() {
    return voyages;
  }

  public LocalDateTime getCompletionTime() {
    return completionTime;
  }

  public void setCompletionTime(LocalDateTime completionTime) {
    this.completionTime = completionTime;
  }

  public String getCompletionTimeValue() {
    return DateConverter.toString(completionTime);
  }

  public String getCompletionTimePattern() {
    return DateConverter.DATE_TIME_FORMAT;
  }

  @PostConstruct
  public void init() {
    List<Cargo> cargos = cargoRepository.findAll();

    trackingIds = new ArrayList<>(cargos.size());

    // List only routed cargo that is not claimed yet.
    cargos
        .stream()
        .filter(
            cargo ->
                !cargo.getItinerary().getLegs().isEmpty()
                    && !(cargo
                        .getDelivery()
                        .getTransportStatus()
                        .sameValueAs(TransportStatus.CLAIMED)))
        .map(cargo -> cargo.getTrackingId().getIdString())
        .forEachOrdered(id -> trackingIds.add(id));

    List<Location> locationList = locationRepository.findAll();
    this.locations = new ArrayList<>(locationList.size());
    locationList.forEach(loc -> {
      String locationCode = loc.getUnLocode().getIdString();
      this.locations.add(locationCode);
    });

    List<Voyage> voyageList = voyageRepository.findAll();
    this.voyages = new ArrayList<>(voyageList.size());
    voyageList.forEach(voyage -> this.voyages.add(voyage.getVoyageNumber().getIdString()));
  }

  public void submit() {
    VoyageNumber voyage;

    TrackingId trackingId = new TrackingId(this.trackingId);
    UnLocode location = new UnLocode(this.location);

    if (eventType.requiresVoyage()) {
      voyage = new VoyageNumber(voyageNumber);
    } else {
      voyage = null;
    }

    HandlingEventRegistrationAttempt attempt =
        new HandlingEventRegistrationAttempt(
            LocalDateTime.now(), completionTime, trackingId, voyage, eventType, location);

    applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
  }
}
