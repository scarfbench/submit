package com.coffeeshop.barista.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class BaristaConsumer {

    private final ObjectMapper mapper = new ObjectMapper();

    @Incoming("barista")
    @Outgoing("order-updates")
    public Message<String> makeCoffee(String orderJson) {
        try {
            System.out.println("[barista] received: " + orderJson);
            Map<String, Object> in = mapper.readValue(orderJson, new TypeReference<Map<String, Object>>() {});

            Object orderId = in.get("orderId");

            Map<String, Object> out = new HashMap<>();
            if (orderId != null) {
                out.put("orderId", orderId);
            }
            out.put("status", "READY");
            out.put("from", "barista");

            return Message.of(mapper.writeValueAsString(out));

        } catch (Exception e) {
            System.err.println("[barista] parse/update error: " + e + " payload=" + orderJson);
            throw new RuntimeException(e);
        }
    }
}
