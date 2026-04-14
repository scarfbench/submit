package org.eclipse.cargotracker.infrastructure.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Our end of the routing service. This is basically a data model translation layer between our
 * domain model and the API put forward by the routing team, which operates in a different context
 * from us.
 */
@Service
public class ExternalRoutingService implements RoutingService {

  private static final Logger logger = Logger.getLogger(ExternalRoutingService.class.getName());

  @Value("${app.graphTraversalUrl}")
  private String graphTraversalUrl;

  private RestTemplate restTemplate;

  @Autowired private LocationRepository locationRepository;
  @Autowired private VoyageRepository voyageRepository;

  @PostConstruct
  public void init() {
    restTemplate = new RestTemplate();
  }

  @Override
  public List<Itinerary> fetchRoutesForSpecification(RouteSpecification routeSpecification) {
    // The RouteSpecification is picked apart and adapted to the external API.
    String origin = routeSpecification.getOrigin().getUnLocode().getIdString();
    String destination = routeSpecification.getDestination().getUnLocode().getIdString();

    String url =
        UriComponentsBuilder.fromHttpUrl(graphTraversalUrl)
            .queryParam("origin", origin)
            .queryParam("destination", destination)
            .toUriString();

    ResponseEntity<List<TransitPath>> response =
        restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TransitPath>>() {});

    List<TransitPath> transitPaths = response.getBody();
    if (transitPaths == null) {
      return new ArrayList<>();
    }

    // The returned result is then translated back into our domain model.
    List<Itinerary> itineraries = new ArrayList<>();

    // Use the specification to safe-guard against invalid itineraries
    transitPaths.stream()
        .map(this::toItinerary)
        .forEach(
            itinerary -> {
              if (routeSpecification.isSatisfiedBy(itinerary)) {
                itineraries.add(itinerary);
              } else {
                logger.log(
                    Level.FINE, "Received itinerary that did not satisfy the route specification");
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
