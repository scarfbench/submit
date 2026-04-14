package io.quarkuscoffeeshop.coffeeshop.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/dashboard")
public class DashboardEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardEndpoint.class);

    private final DashboardService dashboardService;

    public DashboardEndpoint(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter dashboardStream() {
        LOGGER.debug("Client connected to dashboard stream");
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        dashboardService.registerEmitter(emitter);
        // Send an initial event to flush the response headers
        try {
            emitter.send(SseEmitter.event().name("connected").data("{}"));
        } catch (Exception e) {
            LOGGER.warn("Failed to send initial SSE event", e);
        }
        return emitter;
    }

}
