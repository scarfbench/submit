package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.OrderRepository;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.CdiEventBus;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.*;
import static io.quarkuscoffeeshop.utils.JsonUtil.fromJsonToOrderUp;
import static io.quarkuscoffeeshop.utils.JsonUtil.toJson;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {

    Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Inject
    CdiEventBus eventBus;

    @Inject
    OrderRepository orderRepository;

    @PostConstruct
    void init() {
        eventBus.register(ORDERS_UP, this::handleOrderUp);
    }

    private void handleOrderUp(String json) {
        onOrderUp(json);
    }

    @Override
    @Transactional
    public void onOrderIn(final PlaceOrderCommand placeOrderCommand) {
        LOGGER.debug("PlaceOrderCommand received: {}", placeOrderCommand);
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);

        orderRepository.persist(orderEventResult.getOrder());

        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            eventBus.publish(WEB_UPDATES, JsonUtil.toJson(orderUpdate));
            LOGGER.debug("sent web update: {}", orderUpdate);
        });
        if (orderEventResult.getBaristaTickets().isPresent()) {
            orderEventResult.getBaristaTickets().get().forEach(baristaTicket -> {
                eventBus.send(BARISTA_IN, JsonUtil.toJson(baristaTicket));
                LOGGER.debug("sent to barista: {}", baristaTicket);
            });
        }
        if (orderEventResult.getKitchenTickets().isPresent()) {
            orderEventResult.getKitchenTickets().get().forEach(kitchenTicket -> {
                eventBus.send(KITCHEN_IN, JsonUtil.toJson(kitchenTicket));
                LOGGER.debug("sent to kitchen: {}", kitchenTicket);
            });
        }
    }

    @Override
    @Transactional
    public void onOrderUp(final String orderUpJson) {
        LOGGER.debug("order up message: {}", orderUpJson);
        OrderUp orderUp = fromJsonToOrderUp(orderUpJson);

        Order order = orderRepository.findById(orderUp.orderId);
        OrderEventResult orderEventResult = order.apply(orderUp);
        orderRepository.persistAndFlush(order);
        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            eventBus.publish(WEB_UPDATES, JsonUtil.toJson(orderUpdate));
        });
    }

    public OrderServiceImpl() {
    }
}
