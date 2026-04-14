package com.coffeeshop.barista.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

@ApplicationScoped
public class BaristaConsumer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Incoming("barista-in")
    @Outgoing("barista-order-updates")
    public String makeCoffee(String orderJson) {
        try {
            System.out.println("[barista] received: " + orderJson);
            Map<String, Object> in = MAPPER.readValue(orderJson, Map.class);

            Object orderId = in.get("orderId");

            ObjectNode out = MAPPER.createObjectNode();
            if (orderId != null) {
                if (orderId instanceof Number) {
                    out.put("orderId", ((Number) orderId).longValue());
                } else {
                    out.put("orderId", orderId.toString());
                }
            }
            out.put("status", "READY");
            out.put("from", "barista");

            return MAPPER.writeValueAsString(out);

        } catch (Exception e) {
            System.err.println("[barista] parse/update error: " + e + " payload=" + orderJson);
            throw new RuntimeException(e);
        }
    }
}
