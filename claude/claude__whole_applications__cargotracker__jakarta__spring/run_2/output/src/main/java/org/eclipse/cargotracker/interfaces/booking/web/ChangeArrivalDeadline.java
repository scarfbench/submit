package org.eclipse.cargotracker.interfaces.booking.web;

import java.io.Serializable;
import java.time.LocalDate;
import org.eclipse.cargotracker.application.util.DateConverter;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Handles changing the cargo destination. Operates against a dedicated service facade, and could
 * easily be rewritten as a thick Swing client. Completely separated from the domain layer, unlike
 * the tracking user interface.
 *
 * <p>In order to successfully keep the domain model shielded from user interface considerations,
 * this approach is generally preferred to the one taken in the tracking controller. However, there
 * is never any one perfect solution for all situations, so we've chosen to demonstrate two
 * polarized ways to build user interfaces.
 *
 * <p>Migrated from JSF to Spring Boot. JSF-specific UI interactions (PrimeFaces) have been removed.
 */
@Component("changeArrivalDeadline")
@Scope("prototype")
public class ChangeArrivalDeadline implements Serializable {

  private static final long serialVersionUID = 1L;

  @Autowired private BookingServiceFacade bookingServiceFacade;

  private String trackingId;
  private CargoRoute cargo;
  private LocalDate arrivalDeadline;

  public String getTrackingId() {
    return trackingId;
  }

  public void setTrackingId(String trackingId) {
    this.trackingId = trackingId;
  }

  public CargoRoute getCargo() {
    return cargo;
  }

  public LocalDate getArrivalDeadline() {
    return arrivalDeadline;
  }

  public void setArrivalDeadline(LocalDate arrivalDeadline) {
    this.arrivalDeadline = arrivalDeadline;
  }

  public void load() {
    cargo = bookingServiceFacade.loadCargoForRouting(trackingId);
    arrivalDeadline = DateConverter.toDate(cargo.getArrivalDeadline());
  }

  public void changeArrivalDeadline() {
    bookingServiceFacade.changeDeadline(trackingId, arrivalDeadline);
    // PrimeFaces dialog closing removed - would be handled by REST API in Spring Boot
  }
}
