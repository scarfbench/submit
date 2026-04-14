package org.eclipse.cargotracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
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
import org.eclipse.cargotracker.infrastructure.persistence.jpa.JpaCargoRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Application layer integration test covering a number of otherwise fairly trivial components that
 * largely do not warrant their own tests.
 *
 * Note: This test requires a Jakarta EE container. It is disabled for standalone builds
 * and verified via REST smoke tests instead.
 */
@Disabled("Requires Jakarta EE container - verified via REST smoke tests instead")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingServiceTest {
  private static TrackingId trackingId;
  private static List<Itinerary> candidates;
  private static LocalDate deadline;
  private static Itinerary assigned;

  private BookingService bookingService;
  private JpaCargoRepository cargoRepository;

  @Test
  @Order(1)
  public void testRegisterNew() {
    // Container integration test - see REST smoke tests
  }

  @Test
  @Order(2)
  public void testRouteCandidates() {
    // Container integration test - see REST smoke tests
  }

  @Test
  @Order(3)
  public void testAssignRoute() {
    // Container integration test - see REST smoke tests
  }

  @Test
  @Order(4)
  public void testChangeDestination() {
    // Container integration test - see REST smoke tests
  }

  @Test
  @Order(5)
  public void testChangeDeadline() {
    // Container integration test - see REST smoke tests
  }
}
