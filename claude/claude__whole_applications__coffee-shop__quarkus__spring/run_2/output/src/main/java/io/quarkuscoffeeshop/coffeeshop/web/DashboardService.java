package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.WebUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DashboardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardService.class);

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void registerEmitter(SseEmitter emitter) {
        this.emitters.add(emitter);
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));
    }

    @EventListener
    public void handleWebUpdateEvent(WebUpdateEvent event) {
        LOGGER.debug("Broadcasting web update to {} emitters: {}", emitters.size(), event.getPayload());
        CopyOnWriteArrayList<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .data(event.getPayload())
                        .build());
            } catch (IOException e) {
                LOGGER.error("Error sending SSE event", e);
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
    }
}
