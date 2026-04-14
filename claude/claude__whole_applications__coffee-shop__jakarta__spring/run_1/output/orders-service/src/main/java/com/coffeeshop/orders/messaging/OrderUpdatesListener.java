package com.coffeeshop.orders.messaging;

import com.coffeeshop.common.domain.OrderStatus;
import com.coffeeshop.orders.domain.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;

@Component
public class OrderUpdatesListener {

    private final OrderRepository repo;
    private final ObjectMapper objectMapper;

    public OrderUpdatesListener(OrderRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-updates", groupId = "orders-service")
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

            @SuppressWarnings("unchecked")
            Map<String, Object> evt = objectMapper.readValue(json, Map.class);
            if (evt == null || evt.get("status") == null || evt.get("orderId") == null) {
                System.err.println("[order-updates] ignored event: " + json);
                return;
            }

            Long orderId;
            Object orderIdObj = evt.get("orderId");
            if (orderIdObj instanceof Number) {
                orderId = ((Number) orderIdObj).longValue();
            } else {
                orderId = Long.parseLong(orderIdObj.toString());
            }

            var entity = repo.findById(orderId).orElse(null);
            if (entity == null) return;
            entity.setStatus(OrderStatus.valueOf(evt.get("status").toString().toUpperCase(Locale.ROOT)));
            repo.save(entity);

        } catch (Exception e) {
            System.err.println("[order-updates] parse/update error: " + e + " payload=" + payload);
        }
    }
}
