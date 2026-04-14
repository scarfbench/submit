package com.coffeeshop.orders.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrdersPipeline {

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.enabled:false}")
    private boolean kafkaEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void publishToBarista(OrderCommand cmd) {
        if (!kafkaEnabled || kafkaTemplate == null) {
            System.out.println("[orders] Kafka disabled, skipping barista command: " + cmd);
            return;
        }
        try {
            System.out.println("[orders] sending barista command: " + cmd);
            kafkaTemplate.send("barista-commands", objectMapper.writeValueAsString(cmd));
        } catch (Exception e) {
            System.err.println("[orders] failed to send barista command: " + e);
        }
    }

    public void publishToKitchen(OrderCommand cmd) {
        if (!kafkaEnabled || kafkaTemplate == null) {
            System.out.println("[orders] Kafka disabled, skipping kitchen command: " + cmd);
            return;
        }
        try {
            System.out.println("[orders] sending kitchen command: " + cmd);
            kafkaTemplate.send("kitchen-commands", objectMapper.writeValueAsString(cmd));
        } catch (Exception e) {
            System.err.println("[orders] failed to send kitchen command: " + e);
        }
    }

    // simple DTO for messages
    public record OrderCommand(String target, long orderId, String item, int quantity) {}
}
