package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkuscoffeeshop.coffeeshop.barista.api.Barista;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.BaristaOrderInEvent;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.OrderUpEvent;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaristaOutpostImpl implements Barista {

    static final Logger LOGGER = LoggerFactory.getLogger(BaristaOutpostImpl.class);

    private final ApplicationEventPublisher eventPublisher;
    private final BaristaImpl baristaImpl;

    public BaristaOutpostImpl(ApplicationEventPublisher eventPublisher, BaristaImpl baristaImpl) {
        this.eventPublisher = eventPublisher;
        this.baristaImpl = baristaImpl;
    }

    @EventListener
    @Async
    @Transactional
    public void handleBaristaOrderIn(BaristaOrderInEvent event) {
        LOGGER.debug("BaristaOutpost received order: {}", event.getOrderInJson());
        baristaImpl.onOrderIn(event.getOrderInJson());
    }

    @Override
    public void onOrderIn(String orderInJson) {
        eventPublisher.publishEvent(new BaristaOrderInEvent(this, orderInJson));
    }

    @Override
    public void onRemakeIn(String remakeJson) {
        eventPublisher.publishEvent(new BaristaOrderInEvent(this, remakeJson));
    }

    @Override
    public void onCancelOrder(String cancellationJson) {
        eventPublisher.publishEvent(new BaristaOrderInEvent(this, cancellationJson));
    }
}
