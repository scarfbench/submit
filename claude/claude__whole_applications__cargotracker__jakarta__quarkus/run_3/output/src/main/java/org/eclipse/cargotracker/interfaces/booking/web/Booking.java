package org.eclipse.cargotracker.interfaces.booking.web;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.Location;

/**
 * Booking service - converted from JSF backing bean to CDI bean.
 * Functionality exposed via BookingRestService REST endpoint.
 */
@ApplicationScoped
public class Booking implements Serializable {
  private static final long serialVersionUID = 1L;

  @Inject private BookingServiceFacade bookingServiceFacade;

  public List<Location> getLocations() {
    return bookingServiceFacade.listShippingLocations();
  }

  public String register(String originUnlocode, String destinationUnlocode, LocalDate arrivalDeadline) {
    if (!originUnlocode.equals(destinationUnlocode)) {
      return bookingServiceFacade.bookNewCargo(originUnlocode, destinationUnlocode, arrivalDeadline);
    }
    return null;
  }
}
