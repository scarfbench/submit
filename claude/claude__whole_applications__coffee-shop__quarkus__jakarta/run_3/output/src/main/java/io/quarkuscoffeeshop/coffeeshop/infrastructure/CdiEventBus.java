package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple in-memory event bus replacing Vert.x EventBus.
 * Uses topic-based pub/sub with registered consumers.
 */
@ApplicationScoped
public class CdiEventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdiEventBus.class);

    private final Map<String, List<Consumer<String>>> consumers = new ConcurrentHashMap<>();

    public void register(String topic, Consumer<String> consumer) {
        consumers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(consumer);
        LOGGER.debug("Registered consumer for topic: {}", topic);
    }

    public void publish(String topic, String message) {
        LOGGER.debug("Publishing to topic {}: {}", topic, message);
        List<Consumer<String>> topicConsumers = consumers.get(topic);
        if (topicConsumers != null) {
            topicConsumers.forEach(consumer -> {
                try {
                    consumer.accept(message);
                } catch (Exception e) {
                    LOGGER.error("Error delivering message to consumer on topic {}: {}", topic, e.getMessage());
                }
            });
        }
    }

    public void send(String topic, String message) {
        // For send semantics (point-to-point), just deliver to first consumer
        LOGGER.debug("Sending to topic {}: {}", topic, message);
        List<Consumer<String>> topicConsumers = consumers.get(topic);
        if (topicConsumers != null && !topicConsumers.isEmpty()) {
            try {
                topicConsumers.get(0).accept(message);
            } catch (Exception e) {
                LOGGER.error("Error delivering message to consumer on topic {}: {}", topic, e.getMessage());
            }
        }
    }
}
