package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.OrderRepository;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.BaristaInEvent;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.KitchenInEvent;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.WebUpdateEvent;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import static io.quarkuscoffeeshop.utils.JsonUtil.fromJsonToOrderUp;
import static io.quarkuscoffeeshop.utils.JsonUtil.toJson;

@Service
public class OrderServiceImpl implements OrderService {

    Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(ApplicationEventPublisher applicationEventPublisher, OrderRepository orderRepository) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void onOrderIn(final PlaceOrderCommand placeOrderCommand) {
        LOGGER.debug("PlaceOrderCommand received: {}", placeOrderCommand);
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);

        orderRepository.save(orderEventResult.getOrder());

        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            applicationEventPublisher.publishEvent(new WebUpdateEvent(JsonUtil.toJson(orderUpdate)));
            LOGGER.debug("sent web update: {}", orderUpdate);
        });
        if (orderEventResult.getBaristaTickets().isPresent()) {
            orderEventResult.getBaristaTickets().get().forEach(baristaTicket -> {
                applicationEventPublisher.publishEvent(new BaristaInEvent(JsonUtil.toJson(baristaTicket)));
                LOGGER.debug("sent to barista: {}", baristaTicket);
            });
        }
        if (orderEventResult.getKitchenTickets().isPresent()) {
            orderEventResult.getKitchenTickets().get().forEach(kitchenTicket -> {
                applicationEventPublisher.publishEvent(new KitchenInEvent(JsonUtil.toJson(kitchenTicket)));
                LOGGER.debug("sent to kitchen: {}", kitchenTicket);
            });
        }
    }

    @EventListener(io.quarkuscoffeeshop.coffeeshop.infrastructure.events.OrdersUpEvent.class)
    @Transactional
    public void onOrderUpEvent(final io.quarkuscoffeeshop.coffeeshop.infrastructure.events.OrdersUpEvent event) {
        onOrderUp(event.getPayload());
    }

    @Override
    @Transactional
    public void onOrderUp(final String message) {

        LOGGER.debug("order up message: {}", message);
        OrderUp orderUp = fromJsonToOrderUp(message);

        Order order = orderRepository.findById(orderUp.orderId).orElseThrow(
                () -> new RuntimeException("Order not found: " + orderUp.orderId));
        OrderEventResult orderEventResult = order.apply(orderUp);
        orderRepository.save(order);
        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            applicationEventPublisher.publishEvent(new WebUpdateEvent(JsonUtil.toJson(orderUpdate)));
        });

    }
}
