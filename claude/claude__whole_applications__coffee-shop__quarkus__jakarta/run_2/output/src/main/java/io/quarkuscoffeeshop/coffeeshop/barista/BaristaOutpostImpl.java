package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkuscoffeeshop.coffeeshop.barista.api.Barista;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.CdiEventBus;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.BARISTA_IN;
import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.ORDERS_UP;

@ApplicationScoped
public class BaristaOutpostImpl implements Barista {

    static final Logger LOGGER = LoggerFactory.getLogger(BaristaOutpostImpl.class);

    @Inject
    CdiEventBus eventBus;

    @Inject
    BaristaImpl baristaImpl;

    @PostConstruct
    void init() {
        // Register as consumer for barista-in events
        eventBus.registerConsumer(BARISTA_IN, this::handleBaristaIn);
    }

    private void handleBaristaIn(String message) {
        baristaImpl.onOrderIn(message);
    }

    @Override
    public void onOrderIn(final String orderInJson) {
        baristaImpl.onOrderIn(orderInJson);
    }

    @Override
    public void onRemakeIn(final String remakeJson) {
        baristaImpl.onOrderIn(remakeJson);
    }

    @Override
    public void onCancelOrder(final String cancellationJson) {
        // No-op for now
        LOGGER.debug("Cancel order received: {}", cancellationJson);
    }
}
