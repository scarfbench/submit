package com.coffeeshop.kitchen.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KitchenConsumer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KitchenConsumer(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "kitchen-commands", groupId = "kitchen-service")
    public void cookFood(String orderJson) {
        try {
            System.out.println("[kitchen] received: " + orderJson);
            @SuppressWarnings("unchecked")
            Map<String, Object> in = objectMapper.readValue(orderJson, Map.class);

            Object orderId = in.get("orderId");

            Map<String, Object> out = new HashMap<>();
            out.put("status", "READY");
            out.put("from", "kitchen");
            out.put("orderId", orderId != null ? orderId : -1);

            String response = objectMapper.writeValueAsString(out);
            kafkaTemplate.send("order-updates", response);

        } catch (Exception e) {
            System.err.println("[kitchen] parse/update error: " + e + " payload=" + orderJson);
            throw new RuntimeException(e);
        }
    }
}
