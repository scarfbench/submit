package org.eclipse.cargotracker.domain.service;

import java.util.List;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;

public interface RoutingService {
    List<Itinerary> fetchRoutesForSpecification(RouteSpecification routeSpecification);
}
