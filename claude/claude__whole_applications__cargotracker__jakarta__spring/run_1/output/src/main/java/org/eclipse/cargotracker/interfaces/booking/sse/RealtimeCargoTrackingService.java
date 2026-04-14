package org.eclipse.cargotracker.interfaces.booking.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Server-sent events service for tracking all cargo in real time.
 * Migrated from JAX-RS SSE to Spring SseEmitter.
 */
@Controller
@RequestMapping("/rest/tracking")
public class RealtimeCargoTrackingService {

    private static final Logger logger = Logger.getLogger(RealtimeCargoTrackingService.class.getName());

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping("/cargo")
    public SseEmitter trackCargo() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        // Send all current cargo as initial data
        try {
            List<Cargo> allCargo = cargoRepository.findAll();
            for (Cargo cargo : allCargo) {
                RealtimeCargoTrackingViewAdapter adapter = new RealtimeCargoTrackingViewAdapter(cargo);
                emitter.send(SseEmitter.event()
                    .name("cargo")
                    .data(adapter.toJson(objectMapper)));
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error sending initial cargo data", e);
            emitters.remove(emitter);
            emitter.completeWithError(e);
            return emitter;
        }

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        logger.log(Level.FINEST, "SSE client registered. Total clients: {0}", emitters.size());

        return emitter;
    }

    @EventListener
    public void onCargoUpdated(Cargo cargo) {
        if (cargo == null) {
            return;
        }

        logger.log(Level.FINEST, "Broadcasting cargo update: {0}", cargo.getTrackingId());

        RealtimeCargoTrackingViewAdapter adapter = new RealtimeCargoTrackingViewAdapter(cargo);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("cargo")
                    .data(adapter.toJson(objectMapper)));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error sending SSE event, removing emitter", e);
                emitters.remove(emitter);
                emitter.completeWithError(e);
            }
        }
    }
}
