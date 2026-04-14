package com.coffeeshop.kitchen.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

@ApplicationScoped
public class KitchenConsumer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Incoming("kitchen-in")
    @Outgoing("kitchen-order-updates")
    public String cookFood(String orderJson) {
        try {
            System.out.println("[kitchen] received: " + orderJson);
            Map<String, Object> in = MAPPER.readValue(orderJson, Map.class);

            ObjectNode out = MAPPER.createObjectNode();
            out.put("status", "READY");
            out.put("from", "kitchen");

            Object orderId = in.get("orderId");
            if (orderId instanceof Number) {
                out.put("orderId", ((Number) orderId).longValue());
            } else {
                out.put("orderId", -1L);
            }

            return MAPPER.writeValueAsString(out);

        } catch (Exception e) {
            System.err.println("[kitchen] parse/update error: " + e + " payload=" + orderJson);
            throw new RuntimeException(e);
        }
    }
}
