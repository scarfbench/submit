package org.eclipse.cargotracker.interfaces.booking.sse;

import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.jboss.resteasy.reactive.RestStreamElementType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

/** Server-sent events service for tracking all cargo in real time. */
@ApplicationScoped
@Path("/cargo")
public class RealtimeCargoTrackingService {
  @Inject private Logger logger;
  @Inject private CargoRepository cargoRepository;

  private final BroadcastProcessor<RealtimeCargoTrackingViewAdapter> processor = BroadcastProcessor.create();

  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @RestStreamElementType(MediaType.APPLICATION_JSON)
  public Multi<RealtimeCargoTrackingViewAdapter> tracking() {
    // First emit all current cargo, then stream updates
    Multi<RealtimeCargoTrackingViewAdapter> existing = Multi.createFrom().iterable(
        cargoRepository.findAll().stream()
            .map(RealtimeCargoTrackingViewAdapter::new)
            .toList());
    return Multi.createBy().concatenating().streams(existing, processor);
  }

  public void onCargoUpdated(@ObservesAsync @CargoUpdated Cargo cargo) {
    logger.log(Level.FINEST, "SSE event broadcast for cargo: {0}", cargo);
    processor.onNext(new RealtimeCargoTrackingViewAdapter(cargo));
  }
}
