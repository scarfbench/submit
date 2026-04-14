package com.coffeeshop.orders.messaging;

import com.coffeeshop.common.domain.OrderStatus;
import com.coffeeshop.orders.domain.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
public class OrderUpdatesListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderRepository repo;

    public OrderUpdatesListener(OrderRepository repo) {
        this.repo = repo;
    }

    public static class OrderUpdate {
        public Long orderId;
        public String status;
        public String from;
    }

    @KafkaListener(topics = "order-updates", groupId = "orders-service",
                   autoStartup = "${spring.kafka.enabled:true}")
    @Transactional
    public void apply(String payload) {
        try {
            if (payload == null) return;
            String json = payload.trim();

            // Unwrap if the payload is a *stringified* JSON object.
            int safety = 3;
            while (safety-- > 0
                && json.length() >= 2
                && json.charAt(0) == '"'
                && json.charAt(json.length() - 1) == '"') {
                json = objectMapper.readValue(json, String.class).trim();
            }

            OrderUpdate evt = objectMapper.readValue(json, OrderUpdate.class);
            if (evt == null || evt.status == null || evt.orderId == null) {
                System.err.println("[order-updates] ignored event: " + json);
                return;
            }

            var entity = repo.findById(evt.orderId).orElse(null);
            if (entity == null) return;
            entity.setStatus(OrderStatus.valueOf(evt.status.toUpperCase(Locale.ROOT)));
            repo.save(entity);

        } catch (Exception e) {
            System.err.println("[order-updates] parse/update error: " + e + " payload=" + payload);
        }
    }
}
