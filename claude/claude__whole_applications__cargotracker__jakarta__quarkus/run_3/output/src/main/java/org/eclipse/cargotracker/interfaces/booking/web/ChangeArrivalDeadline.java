package org.eclipse.cargotracker.interfaces.booking.web;

import java.time.LocalDate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;

/**
 * Handles changing the arrival deadline. Converted from JSF backing bean to CDI bean.
 * Functionality exposed via REST endpoints.
 */
@ApplicationScoped
public class ChangeArrivalDeadline {
  @Inject private BookingServiceFacade bookingServiceFacade;

  public void changeDeadline(String trackingId, LocalDate arrivalDeadline) {
    bookingServiceFacade.changeDeadline(trackingId, arrivalDeadline);
  }
}
