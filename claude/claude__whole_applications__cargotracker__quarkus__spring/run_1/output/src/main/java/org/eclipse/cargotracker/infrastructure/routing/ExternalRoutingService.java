package org.eclipse.cargotracker.infrastructure.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import org.eclipse.pathfinder.api.TransitPaths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class ExternalRoutingService implements RoutingService {

  private static final Logger logger = Logger.getLogger(ExternalRoutingService.class.getName());

  @Value("${app.configuration.GraphTraversalUrl}")
  private String graphTraversalUrl;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private VoyageRepository voyageRepository;

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public List<Itinerary> fetchRoutesForSpecification(RouteSpecification routeSpecification) {
    String origin = routeSpecification.getOrigin().getUnLocode().getIdString();
    String destination = routeSpecification.getDestination().getUnLocode().getIdString();

    String url = graphTraversalUrl + "?origin=" + origin + "&destination=" + destination;

    TransitPaths transitPaths = restTemplate.getForObject(url, TransitPaths.class);

    List<Itinerary> itineraries = new ArrayList<>();

    if (transitPaths != null && transitPaths.getTransitPaths() != null) {
      transitPaths.getTransitPaths().stream()
          .map(this::toItinerary)
          .forEach(itinerary -> {
            if (routeSpecification.isSatisfiedBy(itinerary)) {
              itineraries.add(itinerary);
            } else {
              logger.log(Level.FINE, "Received itinerary that did not satisfy the route specification");
            }
          });
    }

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
