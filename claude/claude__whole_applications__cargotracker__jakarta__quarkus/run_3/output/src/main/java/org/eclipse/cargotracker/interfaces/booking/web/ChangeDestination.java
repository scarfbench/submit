package org.eclipse.cargotracker.interfaces.booking.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;

/**
 * Handles changing the cargo destination. Converted from JSF backing bean to CDI bean.
 * Functionality exposed via REST endpoints.
 */
@ApplicationScoped
public class ChangeDestination {
  @Inject private BookingServiceFacade bookingServiceFacade;

  public void changeDestination(String trackingId, String destinationUnlocode) {
    bookingServiceFacade.changeDestination(trackingId, destinationUnlocode);
  }
}
