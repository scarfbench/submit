package com.coffeeshop.barista.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BaristaConsumer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public BaristaConsumer(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "barista-commands", groupId = "barista-service")
    public void makeCoffee(String orderJson) {
        try {
            System.out.println("[barista] received: " + orderJson);
            @SuppressWarnings("unchecked")
            Map<String, Object> in = objectMapper.readValue(orderJson, Map.class);

            Object orderId = in.get("orderId");

            Map<String, Object> out = new HashMap<>();
            if (orderId != null) {
                out.put("orderId", orderId);
            }
            out.put("status", "READY");
            out.put("from", "barista");

            String outJson = objectMapper.writeValueAsString(out);
            kafkaTemplate.send("order-updates", outJson);

        } catch (Exception e) {
            System.err.println("[barista] parse/update error: " + e + " payload=" + orderJson);
            throw new RuntimeException(e);
        }
    }
}
