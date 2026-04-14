package org.eclipse.cargotracker.interfaces.booking.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoStatus;

/**
 * Handles tracking cargo. Converted from JSF backing bean to CDI bean.
 * Functionality exposed via REST endpoints.
 */
@ApplicationScoped
public class Track {
  @Inject private BookingServiceFacade bookingServiceFacade;

  public CargoStatus getCargoStatus(String trackingId) {
    return bookingServiceFacade.loadCargoForTracking(trackingId);
  }
}
