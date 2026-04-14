package com.coffeeshop.orders.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrdersPipeline {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrdersPipeline(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishToBarista(OrderCommand cmd) {
        try {
            System.out.println("[orders] sending barista command: " + cmd);
            String json = objectMapper.writeValueAsString(cmd);
            kafkaTemplate.send("barista-commands", json)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.err.println("[orders] Failed to send barista command: " + ex.getMessage());
                    }
                });
        } catch (JsonProcessingException e) {
            System.err.println("[orders] Failed to serialize barista command: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[orders] Kafka send error (barista): " + e.getMessage());
        }
    }

    public void publishToKitchen(OrderCommand cmd) {
        try {
            System.out.println("[orders] sending kitchen command: " + cmd);
            String json = objectMapper.writeValueAsString(cmd);
            kafkaTemplate.send("kitchen-commands", json)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.err.println("[orders] Failed to send kitchen command: " + ex.getMessage());
                    }
                });
        } catch (JsonProcessingException e) {
            System.err.println("[orders] Failed to serialize kitchen command: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[orders] Kafka send error (kitchen): " + e.getMessage());
        }
    }

    // simple DTO for messages
    public record OrderCommand(String target, long orderId, String item, int quantity) {}
}
