package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkuscoffeeshop.coffeeshop.barista.api.Barista;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.OrdersUpEvent;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.BaristaInEvent;
import jakarta.transaction.Transactional;

@Service
public class BaristaOutpostImpl implements Barista {

    static final Logger LOGGER = LoggerFactory.getLogger(BaristaOutpostImpl.class);

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ApplicationEventPublisher applicationEventPublisher;

    public BaristaOutpostImpl(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener(BaristaInEvent.class)
    @Transactional
    public void onOrderIn(final BaristaInEvent event) {
        String orderInJson = event.getPayload();
        OrderIn orderIn = JsonUtil.fromJsonToOrderIn(orderInJson);
        if (kafkaTemplate != null) {
            kafkaTemplate.send("barista-outpost", JsonUtil.toJson(orderIn));
            LOGGER.debug("Sent to Kafka barista-outpost: {}", orderIn);
        } else {
            LOGGER.warn("KafkaTemplate not available, skipping Kafka send");
        }
    }

    @Override
    public void onOrderIn(String orderInJson) {
        OrderIn orderIn = JsonUtil.fromJsonToOrderIn(orderInJson);
        if (kafkaTemplate != null) {
            kafkaTemplate.send("barista-outpost", JsonUtil.toJson(orderIn));
            LOGGER.debug("Sent to Kafka barista-outpost: {}", orderIn);
        } else {
            LOGGER.warn("KafkaTemplate not available, skipping Kafka send");
        }
    }

    @KafkaListener(topics = "orders-up", autoStartup = "${kafka.enabled:false}")
    @Transactional
    public void onOrderUp(String orderUpJson) {
        OrderUp orderUp = JsonUtil.fromJson(orderUpJson, OrderUp.class);
        LOGGER.debug("OrderUp: {}", orderUp);
        applicationEventPublisher.publishEvent(new OrdersUpEvent(JsonUtil.toJson(orderUp)));
    }

}
