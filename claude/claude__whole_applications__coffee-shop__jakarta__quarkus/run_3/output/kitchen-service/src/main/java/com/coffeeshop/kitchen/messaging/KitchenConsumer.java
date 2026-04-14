package com.coffeeshop.kitchen.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class KitchenConsumer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Incoming("kitchen")          // channel that reads topic: kitchen-commands
    @Outgoing("order-updates")    // channel that writes topic: order-updates
    public String cookFood(String orderJson) {
        try {
            System.out.println("[kitchen] received: " + orderJson);
            Map<String, Object> in = MAPPER.readValue(orderJson, new TypeReference<Map<String, Object>>() {});

            Object orderId = in.get("orderId");

            Map<String, Object> out = new HashMap<>();
            out.put("status", "READY");
            out.put("from", "kitchen");
            out.put("orderId", orderId != null ? ((Number) orderId).longValue() : -1L);

            return MAPPER.writeValueAsString(out);
        } catch (Exception e) {
            System.err.println("[kitchen] parse/update error: " + e + " payload=" + orderJson);
            throw new RuntimeException(e);
        }
    }
}
