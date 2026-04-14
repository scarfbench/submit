package org.eclipse.cargotracker.interfaces.booking.sse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.infrastructure.persistence.jpa.JpaCargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Server-sent events service for tracking all cargo in real time. */
@RestController
@RequestMapping("/rest/cargo")
public class RealtimeCargoTrackingService {

  private static final Logger logger = Logger.getLogger(RealtimeCargoTrackingService.class.getName());

  @Autowired
  private JpaCargoRepository cargoRepository;

  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter tracking() {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));

    // Send current state
    try {
      for (Cargo cargo : cargoRepository.listAll()) {
        emitter.send(SseEmitter.event().data(new RealtimeCargoTrackingViewAdapter(cargo)));
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, "Error sending initial SSE data", e);
      emitter.completeWithError(e);
    }

    return emitter;
  }

  @EventListener
  public void onCargoUpdated(Cargo cargo) {
    logger.log(Level.FINEST, "SSE event broadcast for cargo: {0}", cargo);
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().data(new RealtimeCargoTrackingViewAdapter(cargo)));
      } catch (IOException e) {
        emitters.remove(emitter);
      }
    }
  }
}
