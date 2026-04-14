package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.OrderRepository;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.BaristaInEvent;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.KitchenInEvent;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.OrdersUpEvent;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.WebUpdateEvent;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import static io.quarkuscoffeeshop.utils.JsonUtil.fromJsonToOrderUp;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {

    Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Inject
    Event<WebUpdateEvent> webUpdateEvent;

    @Inject
    Event<BaristaInEvent> baristaInEvent;

    @Inject
    Event<KitchenInEvent> kitchenInEvent;

    @Inject
    OrderRepository orderRepository;

    @Override
    @Transactional
    public void onOrderIn(final PlaceOrderCommand placeOrderCommand) {
        LOGGER.debug("PlaceOrderCommand received: {}", placeOrderCommand);
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);

        orderRepository.persist(orderEventResult.getOrder());

        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            webUpdateEvent.fire(new WebUpdateEvent(JsonUtil.toJson(orderUpdate)));
            LOGGER.debug("sent web update: {}", orderUpdate);
        });
        if (orderEventResult.getBaristaTickets().isPresent()) {
            orderEventResult.getBaristaTickets().get().forEach(baristaTicket -> {
                baristaInEvent.fire(new BaristaInEvent(JsonUtil.toJson(baristaTicket)));
                LOGGER.debug("sent to barista: {}", baristaTicket);
            });
        }
        if (orderEventResult.getKitchenTickets().isPresent()) {
            orderEventResult.getKitchenTickets().get().forEach(kitchenTicket -> {
                kitchenInEvent.fire(new KitchenInEvent(JsonUtil.toJson(kitchenTicket)));
                LOGGER.debug("sent to kitchen: {}", kitchenTicket);
            });
        }
    }

    @Override
    @Transactional
    public void onOrderUp(final OrdersUpEvent event) {

        LOGGER.debug("order up message: {}", event.getJson());
        OrderUp orderUp = fromJsonToOrderUp(event.getJson());

        Order order = orderRepository.findById(orderUp.orderId);
        if (order != null) {
            OrderEventResult orderEventResult = order.apply(orderUp);
            orderRepository.persistAndFlush(order);
            orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
                webUpdateEvent.fire(new WebUpdateEvent(JsonUtil.toJson(orderUpdate)));
            });
        }
    }

    // CDI Observer for OrdersUp events
    public void observeOrdersUp(@Observes OrdersUpEvent event) {
        onOrderUp(event);
    }

    public OrderServiceImpl() {
    }
}
