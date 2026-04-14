package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusService;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class BaristaOutpostImpl {

    static final Logger LOGGER = LoggerFactory.getLogger(BaristaOutpostImpl.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventBusService eventBusService;

    public BaristaOutpostImpl(KafkaTemplate<String, String> kafkaTemplate,
                              EventBusService eventBusService) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventBusService = eventBusService;
    }

    @PostConstruct
    public void init() {
        eventBusService.registerBaristaInHandler(this::onBaristaIn);
    }

    public void onBaristaIn(final String orderInJson) {
        LOGGER.debug("Sending to barista-in kafka topic: {}", orderInJson);
        kafkaTemplate.send("barista-in", orderInJson);
    }

    @KafkaListener(topics = "orders-up", groupId = "coffeeshop-group")
    public void onOrderUp(String orderUpJson) {
        LOGGER.debug("OrderUp received from Kafka: {}", orderUpJson);
        eventBusService.sendToOrdersUp(orderUpJson);
    }
}
