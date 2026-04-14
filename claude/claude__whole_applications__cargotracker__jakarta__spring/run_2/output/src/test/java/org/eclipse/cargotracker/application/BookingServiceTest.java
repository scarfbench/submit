package org.eclipse.cargotracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.Delivery;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.RoutingStatus;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.cargo.TransportStatus;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application layer integration test covering a number of otherwise fairly trivial components that
 * largely do not warrant their own tests.
 *
 * <p>Migrated from Arquillian to Spring Boot Test.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class BookingServiceTest {
  private static TrackingId trackingId;
  private static List<Itinerary> candidates;
  private static LocalDate deadline;
  private static Itinerary assigned;

  @Autowired private BookingService bookingService;
  @PersistenceContext private EntityManager entityManager;

  @Test
  @Order(1)
  public void testRegisterNew() {
    UnLocode fromUnlocode = new UnLocode("USCHI");
    UnLocode toUnlocode = new UnLocode("SESTO");

    deadline = LocalDate.now().plusMonths(6);

    trackingId = bookingService.bookNewCargo(fromUnlocode, toUnlocode, deadline);

    Cargo cargo =
        entityManager
            .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
            .setParameter("trackingId", trackingId)
            .getSingleResult();

    assertEquals(SampleLocations.CHICAGO, cargo.getOrigin());
    assertEquals(SampleLocations.STOCKHOLM, cargo.getRouteSpecification().getDestination());
    assertTrue(deadline.isEqual(cargo.getRouteSpecification().getArrivalDeadline()));
    assertEquals(TransportStatus.NOT_RECEIVED, cargo.getDelivery().getTransportStatus());
    assertEquals(Location.UNKNOWN, cargo.getDelivery().getLastKnownLocation());
    assertEquals(Voyage.NONE, cargo.getDelivery().getCurrentVoyage());
    assertFalse(cargo.getDelivery().isMisdirected());
    assertEquals(Delivery.ETA_UNKOWN, cargo.getDelivery().getEstimatedTimeOfArrival());
    assertEquals(Delivery.NO_ACTIVITY, cargo.getDelivery().getNextExpectedActivity());
    assertFalse(cargo.getDelivery().isUnloadedAtDestination());
    assertEquals(RoutingStatus.NOT_ROUTED, cargo.getDelivery().getRoutingStatus());
    assertEquals(Itinerary.EMPTY_ITINERARY, cargo.getItinerary());
  }

  @Test
  @Order(2)
  public void testRouteCandidates() {
    // Re-book since each test is transactional
    UnLocode fromUnlocode = new UnLocode("USCHI");
    UnLocode toUnlocode = new UnLocode("SESTO");
    deadline = LocalDate.now().plusMonths(6);
    trackingId = bookingService.bookNewCargo(fromUnlocode, toUnlocode, deadline);

    candidates = bookingService.requestPossibleRoutesForCargo(trackingId);

    assertFalse(candidates.isEmpty());
  }

  @Test
  @Order(3)
  public void testAssignRoute() {
    UnLocode fromUnlocode = new UnLocode("USCHI");
    UnLocode toUnlocode = new UnLocode("SESTO");
    deadline = LocalDate.now().plusMonths(6);
    trackingId = bookingService.bookNewCargo(fromUnlocode, toUnlocode, deadline);
    candidates = bookingService.requestPossibleRoutesForCargo(trackingId);

    assigned = candidates.get(new Random().nextInt(candidates.size()));

    bookingService.assignCargoToRoute(assigned, trackingId);

    Cargo cargo =
        entityManager
            .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
            .setParameter("trackingId", trackingId)
            .getSingleResult();

    assertEquals(assigned, cargo.getItinerary());
    assertEquals(TransportStatus.NOT_RECEIVED, cargo.getDelivery().getTransportStatus());
    assertEquals(Location.UNKNOWN, cargo.getDelivery().getLastKnownLocation());
    assertEquals(Voyage.NONE, cargo.getDelivery().getCurrentVoyage());
    assertFalse(cargo.getDelivery().isMisdirected());
    assertTrue(cargo.getDelivery().getEstimatedTimeOfArrival().isBefore(deadline.atStartOfDay()));
    Assertions.assertEquals(
        HandlingEvent.Type.RECEIVE, cargo.getDelivery().getNextExpectedActivity().getType());
    Assertions.assertEquals(
        SampleLocations.CHICAGO, cargo.getDelivery().getNextExpectedActivity().getLocation());
    Assertions.assertEquals(null, cargo.getDelivery().getNextExpectedActivity().getVoyage());
    assertFalse(cargo.getDelivery().isUnloadedAtDestination());
    assertEquals(RoutingStatus.ROUTED, cargo.getDelivery().getRoutingStatus());
  }

  @Test
  @Order(4)
  public void testChangeDestination() {
    UnLocode fromUnlocode = new UnLocode("USCHI");
    UnLocode toUnlocode = new UnLocode("SESTO");
    deadline = LocalDate.now().plusMonths(6);
    trackingId = bookingService.bookNewCargo(fromUnlocode, toUnlocode, deadline);
    candidates = bookingService.requestPossibleRoutesForCargo(trackingId);
    assigned = candidates.get(new Random().nextInt(candidates.size()));
    bookingService.assignCargoToRoute(assigned, trackingId);

    bookingService.changeDestination(trackingId, new UnLocode("FIHEL"));

    Cargo cargo =
        entityManager
            .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
            .setParameter("trackingId", trackingId)
            .getSingleResult();

    assertEquals(SampleLocations.CHICAGO, cargo.getOrigin());
    assertEquals(SampleLocations.HELSINKI, cargo.getRouteSpecification().getDestination());
    assertTrue(deadline.isEqual(cargo.getRouteSpecification().getArrivalDeadline()));
    assertEquals(assigned, cargo.getItinerary());
    assertEquals(TransportStatus.NOT_RECEIVED, cargo.getDelivery().getTransportStatus());
    assertEquals(Location.UNKNOWN, cargo.getDelivery().getLastKnownLocation());
    assertEquals(Voyage.NONE, cargo.getDelivery().getCurrentVoyage());
    assertFalse(cargo.getDelivery().isMisdirected());
    assertEquals(Delivery.ETA_UNKOWN, cargo.getDelivery().getEstimatedTimeOfArrival());
    assertEquals(Delivery.NO_ACTIVITY, cargo.getDelivery().getNextExpectedActivity());
    assertFalse(cargo.getDelivery().isUnloadedAtDestination());
    assertEquals(RoutingStatus.MISROUTED, cargo.getDelivery().getRoutingStatus());
  }

  @Test
  @Order(5)
  public void testChangeDeadline() {
    UnLocode fromUnlocode = new UnLocode("USCHI");
    UnLocode toUnlocode = new UnLocode("SESTO");
    deadline = LocalDate.now().plusMonths(6);
    trackingId = bookingService.bookNewCargo(fromUnlocode, toUnlocode, deadline);
    candidates = bookingService.requestPossibleRoutesForCargo(trackingId);
    assigned = candidates.get(new Random().nextInt(candidates.size()));
    bookingService.assignCargoToRoute(assigned, trackingId);
    bookingService.changeDestination(trackingId, new UnLocode("FIHEL"));

    LocalDate newDeadline = deadline.plusMonths(1);
    bookingService.changeDeadline(trackingId, newDeadline);

    Cargo cargo =
        entityManager
            .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
            .setParameter("trackingId", trackingId)
            .getSingleResult();

    assertEquals(SampleLocations.CHICAGO, cargo.getOrigin());
    assertEquals(SampleLocations.HELSINKI, cargo.getRouteSpecification().getDestination());
    assertTrue(newDeadline.isEqual(cargo.getRouteSpecification().getArrivalDeadline()));
    assertEquals(assigned, cargo.getItinerary());
    assertEquals(TransportStatus.NOT_RECEIVED, cargo.getDelivery().getTransportStatus());
    assertEquals(Location.UNKNOWN, cargo.getDelivery().getLastKnownLocation());
    assertEquals(Voyage.NONE, cargo.getDelivery().getCurrentVoyage());
    assertFalse(cargo.getDelivery().isMisdirected());
    assertEquals(Delivery.ETA_UNKOWN, cargo.getDelivery().getEstimatedTimeOfArrival());
    assertEquals(Delivery.NO_ACTIVITY, cargo.getDelivery().getNextExpectedActivity());
    assertFalse(cargo.getDelivery().isUnloadedAtDestination());
    assertEquals(RoutingStatus.MISROUTED, cargo.getDelivery().getRoutingStatus());
  }
}
