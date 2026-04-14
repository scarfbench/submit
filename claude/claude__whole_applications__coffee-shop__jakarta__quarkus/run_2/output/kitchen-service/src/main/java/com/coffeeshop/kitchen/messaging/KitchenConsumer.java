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

    private final ObjectMapper mapper = new ObjectMapper();

    @Incoming("kitchen")
    @Outgoing("order-updates")
    public String cookFood(String orderJson) {
        try {
            System.out.println("[kitchen] received: " + orderJson);
            Map<String, Object> in = mapper.readValue(orderJson, new TypeReference<Map<String, Object>>() {});

            Map<String, Object> out = new HashMap<>();
            out.put("status", "READY");
            out.put("from", "kitchen");
            out.put("orderId", in.containsKey("orderId") ? in.get("orderId") : -1);

            return mapper.writeValueAsString(out);

        } catch (Exception e) {
            System.err.println("[kitchen] parse/update error: " + e + " payload=" + orderJson);
            throw new RuntimeException(e);
        }
    }
}
