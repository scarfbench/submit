package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/dashboard")
public class DashboardEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardEndpoint.class);

    private final EventBusService eventBusService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public DashboardEndpoint(EventBusService eventBusService) {
        this.eventBusService = eventBusService;
    }

    @PostConstruct
    public void init() {
        eventBusService.registerWebUpdateHandler(this::broadcast);
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter dashboardStream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    private void broadcast(String data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
