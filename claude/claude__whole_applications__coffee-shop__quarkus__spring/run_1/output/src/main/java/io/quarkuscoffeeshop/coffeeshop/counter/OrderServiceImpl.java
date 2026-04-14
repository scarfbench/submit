package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.OrderRepository;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusService;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.quarkuscoffeeshop.utils.JsonUtil.fromJsonToOrderUp;
import static io.quarkuscoffeeshop.utils.JsonUtil.toJson;

@Service
public class OrderServiceImpl implements OrderService {

    Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final EventBusService eventBusService;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(EventBusService eventBusService, OrderRepository orderRepository) {
        this.eventBusService = eventBusService;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void onOrderIn(final PlaceOrderCommand placeOrderCommand) {
        LOGGER.debug("PlaceOrderCommand received: {}", placeOrderCommand);
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);

        orderRepository.save(orderEventResult.getOrder());

        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            eventBusService.publishWebUpdate(JsonUtil.toJson(orderUpdate));
            LOGGER.debug("sent web update: {}", orderUpdate);
        });
        if (orderEventResult.getBaristaTickets().isPresent()) {
            orderEventResult.getBaristaTickets().get().forEach(baristaTicket -> {
                eventBusService.sendToBaristaIn(JsonUtil.toJson(baristaTicket));
                LOGGER.debug("sent to barista: {}", baristaTicket);
            });
        }
        if (orderEventResult.getKitchenTickets().isPresent()) {
            orderEventResult.getKitchenTickets().get().forEach(kitchenTicket -> {
                eventBusService.sendToKitchenIn(JsonUtil.toJson(kitchenTicket));
                LOGGER.debug("sent to kitchen: {}", kitchenTicket);
            });
        }
    }

    @Override
    @Transactional
    public void onOrderUp(final String orderUpJson) {
        LOGGER.debug("order up message: {}", orderUpJson);
        OrderUp orderUp = fromJsonToOrderUp(orderUpJson);

        orderRepository.findById(orderUp.orderId).ifPresent(order -> {
            OrderEventResult orderEventResult = order.apply(orderUp);
            orderRepository.save(order);
            orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
                eventBusService.publishWebUpdate(JsonUtil.toJson(orderUpdate));
            });
        });
    }
}
