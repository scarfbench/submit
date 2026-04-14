package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.*;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final ApplicationEventPublisher eventPublisher;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(ApplicationEventPublisher eventPublisher, OrderRepository orderRepository) {
        this.eventPublisher = eventPublisher;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void onOrderIn(final PlaceOrderCommand placeOrderCommand) {
        LOGGER.debug("PlaceOrderCommand received: {}", placeOrderCommand);
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);

        orderRepository.save(orderEventResult.getOrder());

        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            eventPublisher.publishEvent(new WebUpdateEvent(this, JsonUtil.toJson(orderUpdate)));
            LOGGER.debug("sent web update: {}", orderUpdate);
        });
        if (orderEventResult.getBaristaTickets().isPresent()) {
            orderEventResult.getBaristaTickets().get().forEach(baristaTicket -> {
                eventPublisher.publishEvent(new BaristaOrderInEvent(this, JsonUtil.toJson(baristaTicket)));
                LOGGER.debug("sent to barista: {}", baristaTicket);
            });
        }
        if (orderEventResult.getKitchenTickets().isPresent()) {
            orderEventResult.getKitchenTickets().get().forEach(kitchenTicket -> {
                eventPublisher.publishEvent(new KitchenOrderInEvent(this, JsonUtil.toJson(kitchenTicket)));
                LOGGER.debug("sent to kitchen: {}", kitchenTicket);
            });
        }
    }

    @EventListener
    @Async
    @Transactional
    public void handleOrderUp(OrderUpEvent event) {
        onOrderUp(event.getOrderUpJson());
    }

    @Override
    @Transactional
    public void onOrderUp(String orderUpJson) {
        LOGGER.debug("order up message: {}", orderUpJson);
        OrderUp orderUp = JsonUtil.fromJsonToOrderUp(orderUpJson);

        Order order = orderRepository.findById(orderUp.orderId).orElse(null);
        if (order != null) {
            OrderEventResult orderEventResult = order.apply(orderUp);
            orderRepository.save(order);
            orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
                eventPublisher.publishEvent(new WebUpdateEvent(this, JsonUtil.toJson(orderUpdate)));
            });
        } else {
            LOGGER.warn("Order not found for id: {}", orderUp.orderId);
        }
    }
}
