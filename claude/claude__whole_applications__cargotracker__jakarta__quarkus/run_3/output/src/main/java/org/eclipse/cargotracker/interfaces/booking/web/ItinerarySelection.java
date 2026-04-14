package org.eclipse.cargotracker.interfaces.booking.web;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.RouteCandidate;

/**
 * Handles itinerary selection. Converted from JSF backing bean to CDI bean.
 * Functionality exposed via REST endpoints.
 */
@ApplicationScoped
public class ItinerarySelection {
  @Inject private BookingServiceFacade bookingServiceFacade;

  public List<RouteCandidate> getRouteCandidates(String trackingId) {
    return bookingServiceFacade.requestPossibleRoutesForCargo(trackingId);
  }

  public void assignItinerary(String trackingId, RouteCandidate route) {
    bookingServiceFacade.assignCargoToRoute(trackingId, route);
  }
}
