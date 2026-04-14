package org.eclipse.cargotracker.interfaces.booking.sse;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.infrastructure.events.cdi.CargoUpdated;

/**
 * Real-time cargo tracking service.
 * Simplified for Quarkus - provides a REST endpoint instead of SSE.
 */
@ApplicationScoped
@Path("/cargo")
public class RealtimeCargoTrackingService {

    @Inject
    Logger logger;

    @Inject
    CargoRepository cargoRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RealtimeCargoTrackingViewAdapter> tracking() {
        return cargoRepository.findAll().stream()
            .map(RealtimeCargoTrackingViewAdapter::new)
            .collect(Collectors.toList());
    }

    public void onCargoUpdated(@ObservesAsync @CargoUpdated Cargo cargo) {
        logger.log(Level.FINEST, "Cargo updated event received for: {0}", cargo);
    }
}
