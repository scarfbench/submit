package com.coffeeshop.orders.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class OrdersPipeline {

    @Inject
    @Channel("barista")
    Emitter<String> barista;

    @Inject
    @Channel("kitchen")
    Emitter<String> kitchen;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void publishToBarista(OrderCommand cmd) {
        try {
            String json = MAPPER.writeValueAsString(cmd);
            System.out.println("[orders] sending barista command: " + json);
            barista.send(json).toCompletableFuture().exceptionally(ex -> {
                System.err.println("[orders] failed to send barista command (Kafka unavailable): " + ex.getMessage());
                return null;
            });
        } catch (Exception e) {
            System.err.println("[orders] failed to publish barista command: " + e.getMessage());
        }
    }

    public void publishToKitchen(OrderCommand cmd) {
        try {
            String json = MAPPER.writeValueAsString(cmd);
            System.out.println("[orders] sending kitchen command: " + json);
            kitchen.send(json).toCompletableFuture().exceptionally(ex -> {
                System.err.println("[orders] failed to send kitchen command (Kafka unavailable): " + ex.getMessage());
                return null;
            });
        } catch (Exception e) {
            System.err.println("[orders] failed to publish kitchen command: " + e.getMessage());
        }
    }

    // simple DTO for messages
    public record OrderCommand(String target, long orderId, String item, int quantity) {}
}
