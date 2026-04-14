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

@ApplicationScoped
public class CdiEventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdiEventBus.class);

    private final Map<String, List<Consumer<String>>> consumers = new ConcurrentHashMap<>();

    public void registerConsumer(String address, Consumer<String> consumer) {
        consumers.computeIfAbsent(address, k -> new CopyOnWriteArrayList<>()).add(consumer);
        LOGGER.debug("Registered consumer for address: {}", address);
    }

    public void publish(String address, String message) {
        LOGGER.debug("Publishing to {}: {}", address, message);
        List<Consumer<String>> addressConsumers = consumers.get(address);
        if (addressConsumers != null) {
            for (Consumer<String> consumer : addressConsumers) {
                try {
                    consumer.accept(message);
                } catch (Exception e) {
                    LOGGER.error("Error in consumer for address {}: {}", address, e.getMessage());
                }
            }
        }
    }

    public void send(String address, String message) {
        // send delivers to one consumer, publish delivers to all
        // For simplicity, treat send same as publish
        publish(address, message);
    }
}
