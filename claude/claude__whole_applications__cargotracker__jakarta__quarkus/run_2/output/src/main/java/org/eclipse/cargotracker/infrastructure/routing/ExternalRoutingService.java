package org.eclipse.cargotracker.infrastructure.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.Leg;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.domain.service.RoutingService;
import org.eclipse.pathfinder.api.GraphTraversalService;
import org.eclipse.pathfinder.api.TransitEdge;
import org.eclipse.pathfinder.api.TransitPath;

/**
 * Our end of the routing service. This is basically a data model translation layer between our
 * domain model and the API put forward by the routing team, which operates in a different context
 * from us.
 *
 * In the Quarkus version, we call the GraphTraversalService directly (in-process)
 * instead of going through REST, since both services are in the same application.
 */
@ApplicationScoped
public class ExternalRoutingService implements RoutingService {

    @Inject
    Logger logger;

    @Inject
    GraphTraversalService graphTraversalService;

    @Inject
    LocationRepository locationRepository;

    @Inject
    VoyageRepository voyageRepository;

    @Override
    public List<Itinerary> fetchRoutesForSpecification(RouteSpecification routeSpecification) {
        String origin = routeSpecification.getOrigin().getUnLocode().getIdString();
        String destination = routeSpecification.getDestination().getUnLocode().getIdString();

        List<TransitPath> transitPaths = graphTraversalService.findShortestPath(
            origin, destination, null);

        List<Itinerary> itineraries = new ArrayList<>();

        transitPaths.stream()
            .map(this::toItinerary)
            .forEach(itinerary -> {
                if (routeSpecification.isSatisfiedBy(itinerary)) {
                    itineraries.add(itinerary);
                } else {
                    logger.log(Level.FINE,
                        "Received itinerary that did not satisfy the route specification");
                }
            });

        return itineraries;
    }

    private Itinerary toItinerary(TransitPath transitPath) {
        List<Leg> legs = transitPath.getTransitEdges().stream()
            .map(this::toLeg)
            .collect(Collectors.toList());
        return new Itinerary(legs);
    }

    private Leg toLeg(TransitEdge edge) {
        return new Leg(
            voyageRepository.find(new VoyageNumber(edge.getVoyageNumber())),
            locationRepository.find(new UnLocode(edge.getFromUnLocode())),
            locationRepository.find(new UnLocode(edge.getToUnLocode())),
            edge.getFromDate(),
            edge.getToDate());
    }
}
