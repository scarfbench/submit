package org.eclipse.cargotracker.interfaces.booking.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRoute;

/**
 * Handles viewing cargo details. Converted from JSF backing bean to CDI bean.
 * Functionality exposed via REST endpoints.
 */
@ApplicationScoped
public class CargoDetails {
  @Inject private BookingServiceFacade bookingServiceFacade;

  public CargoRoute getCargo(String trackingId) {
    return bookingServiceFacade.loadCargoForRouting(trackingId);
  }
}
