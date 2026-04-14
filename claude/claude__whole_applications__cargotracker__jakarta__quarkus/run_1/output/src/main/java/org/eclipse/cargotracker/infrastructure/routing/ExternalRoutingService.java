package org.eclipse.cargotracker.infrastructure.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.Leg;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.domain.service.RoutingService;
import org.eclipse.pathfinder.api.TransitEdge;
import org.eclipse.pathfinder.api.TransitPath;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Transactional
public class ExternalRoutingService implements RoutingService {

  @Inject Logger logger;

  @ConfigProperty(name = "app.graph-traversal-url")
  String graphTraversalUrl;

  private WebTarget graphTraversalResource;

  @Inject LocationRepository locationRepository;
  @Inject VoyageRepository voyageRepository;

  @PostConstruct
  public void init() {
    graphTraversalResource = ClientBuilder.newClient().target(graphTraversalUrl);
  }

  @Override
  public List<Itinerary> fetchRoutesForSpecification(RouteSpecification routeSpecification) {
    String origin = routeSpecification.getOrigin().getUnLocode().getIdString();
    String destination = routeSpecification.getDestination().getUnLocode().getIdString();

    List<TransitPath> transitPaths =
        graphTraversalResource
            .queryParam("origin", origin)
            .queryParam("destination", destination)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(new GenericType<List<TransitPath>>() {});

    List<Itinerary> itineraries = new ArrayList<>();

    transitPaths
        .stream()
        .map(this::toItinerary)
        .forEach(
            itinerary -> {
              if (routeSpecification.isSatisfiedBy(itinerary)) {
                itineraries.add(itinerary);
              } else {
                logger.log(Level.FINE, "Received itinerary that did not satisfy the route specification");
              }
            });

    return itineraries;
  }

  private Itinerary toItinerary(TransitPath transitPath) {
    List<Leg> legs =
        transitPath.getTransitEdges().stream().map(this::toLeg).collect(Collectors.toList());
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
