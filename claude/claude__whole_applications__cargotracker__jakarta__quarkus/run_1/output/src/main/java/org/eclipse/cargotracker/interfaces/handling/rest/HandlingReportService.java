package org.eclipse.cargotracker.interfaces.handling.rest;

import java.time.LocalDateTime;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateConverter;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

@ApplicationScoped
@Path("/handling")
public class HandlingReportService {

  @Inject ApplicationEvents applicationEvents;

  @POST
  @Path("/reports")
  @Consumes({"application/json", "application/xml"})
  @Transactional
  public void submitReport(
      @NotNull(message = "Missing handling report.") @Valid HandlingReport handlingReport) {
    LocalDateTime completionTime = DateConverter.toDateTime(handlingReport.getCompletionTime());
    VoyageNumber voyageNumber = null;

    if (handlingReport.getVoyageNumber() != null) {
      voyageNumber = new VoyageNumber(handlingReport.getVoyageNumber());
    }

    HandlingEvent.Type type = HandlingEvent.Type.valueOf(handlingReport.getEventType());
    UnLocode unLocode = new UnLocode(handlingReport.getUnLocode());
    TrackingId trackingId = new TrackingId(handlingReport.getTrackingId());

    HandlingEventRegistrationAttempt attempt =
        new HandlingEventRegistrationAttempt(
            LocalDateTime.now(), completionTime, trackingId, voyageNumber, type, unLocode);

    applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
  }
}
