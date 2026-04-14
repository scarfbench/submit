package org.eclipse.cargotracker.interfaces.booking.web;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRoute;

/**
 * Handles listing cargo. Converted from JSF backing bean to CDI bean.
 * Functionality exposed via REST endpoints.
 */
@ApplicationScoped
public class ListCargo {
  @Inject private BookingServiceFacade bookingServiceFacade;

  public List<CargoRoute> getCargos() {
    return bookingServiceFacade.listAllCargos();
  }
}
