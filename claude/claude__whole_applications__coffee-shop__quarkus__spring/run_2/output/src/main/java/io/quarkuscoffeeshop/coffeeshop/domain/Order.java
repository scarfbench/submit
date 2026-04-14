package io.quarkuscoffeeshop.coffeeshop.domain;

import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUpdate;
import org.hibernate.annotations.Where;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Entity
@Table(name = "Orders")
public class Order {

    @Transient
    static Logger LOGGER = LoggerFactory.getLogger(Order.class);

    @Id
    @Column(nullable = false, unique = true, name = "order_id")
    private String orderId;

    @Enumerated(EnumType.STRING)
    private OrderSource orderSource;

    private String loyaltyMemberId;

    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private Location location;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "order", cascade = CascadeType.ALL)
    @Where(clause = "line_item_type = 'BARISTA'")
    private List<LineItem> baristaLineItems;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "order", cascade = CascadeType.ALL)
    @Where(clause = "line_item_type = 'KITCHEN'")
    private List<LineItem> kitchenLineItems;

    protected Order() {
    }

    private Order(String orderId) {
        this.orderId = orderId;
        this.timestamp = Instant.now();
    }

    public static OrderEventResult from(final PlaceOrderCommand placeOrderCommand) {

        Order order = new Order(placeOrderCommand.getId());
        order.setOrderSource(placeOrderCommand.getOrderSource());
        order.setLocation(placeOrderCommand.getLocation());
        order.setTimestamp(placeOrderCommand.getTimestamp());
        order.setOrderStatus(OrderStatus.IN_PROGRESS);

        OrderEventResult orderEventResult = new OrderEventResult();

        if (placeOrderCommand.getBaristaItems().isPresent()) {
            LOGGER.debug("createOrderFromCommand adding beverages {}", placeOrderCommand.getBaristaItems().get().size());
            LOGGER.debug("adding Barista LineItems");
            placeOrderCommand.getBaristaItems().get().forEach(commandItem -> {
                LOGGER.debug("createOrderFromCommand adding baristaItem from {}", commandItem.toString());
                LineItem lineItem = new LineItem(commandItem.item, commandItem.name, commandItem.price, ItemStatus.IN_PROGRESS, order);
                lineItem.setLineItemType("BARISTA");
                order.addBaristaLineItem(lineItem);
                LOGGER.debug("added LineItem: {}", order.getBaristaLineItems().get().size());
                orderEventResult.addBaristaTicket(new OrderIn(order.getOrderId(), lineItem.getItemId(), lineItem.getItem(), lineItem.getName()));
                LOGGER.debug("Added Barista Ticket to OrderEventResult: {}", orderEventResult.getBaristaTickets().get().size());
                orderEventResult.addUpdate(new OrderUpdate(order.getOrderId(), lineItem.getItemId(), lineItem.getName(), lineItem.getItem(), OrderStatus.IN_PROGRESS));
                LOGGER.debug("Added Order Update to OrderEventResult: ", orderEventResult.getOrderUpdates().size());
            });
        }
        LOGGER.debug("adding Kitchen LineItems");
        if (placeOrderCommand.getKitchenItems().isPresent()) {
            LOGGER.debug("createOrderFromCommand adding kitchenOrders {}", placeOrderCommand.getKitchenItems().get().size());
            placeOrderCommand.getKitchenItems().get().forEach(commandItem -> {
                LOGGER.debug("createOrderFromCommand adding kitchenItem from {}", commandItem.toString());
                LineItem lineItem = new LineItem(commandItem.item, commandItem.name, commandItem.price, ItemStatus.IN_PROGRESS, order);
                lineItem.setLineItemType("KITCHEN");
                order.addKitchenLineItem(lineItem);
                orderEventResult.addKitchenTicket(new OrderIn(order.getOrderId(), lineItem.getItemId(), lineItem.getItem(), lineItem.getName()));
                orderEventResult.addUpdate(new OrderUpdate(order.getOrderId(), lineItem.getItemId(), lineItem.getName(), lineItem.getItem(), OrderStatus.IN_PROGRESS));
            });
        }

        orderEventResult.setOrder(order);
        LOGGER.debug("returning {}", orderEventResult);
        return orderEventResult;
    }

    public OrderEventResult apply(final OrderUp orderUp) {

        LOGGER.debug("applying orderUp: {}", orderUp);
        OrderEventResult orderEventResult = new OrderEventResult();

        orderEventResult.addUpdate(new OrderUpdate(
                orderUp.orderId, orderUp.itemId, orderUp.name, orderUp.item,
                OrderStatus.FULFILLED, orderUp.madeBy));

        if (this.getBaristaLineItems().isPresent()) {
            this.getBaristaLineItems().get().forEach(baristaLineItem -> {
                if (baristaLineItem.getItemId().equals(orderUp.itemId)) {
                    baristaLineItem.setItemStatus(ItemStatus.FULFILLED);
                    orderEventResult.addUpdate(new OrderUpdate(orderUp.orderId, orderUp.itemId, orderUp.name, orderUp.item, OrderStatus.FULFILLED, orderUp.madeBy));
                }
            });
        }
        if (this.getKitchenLineItems().isPresent()) {
            this.getKitchenLineItems().get().forEach(kitchenLineItem -> {
                if (kitchenLineItem.getItemId().equals(orderUp.itemId)) {
                    kitchenLineItem.setItemStatus(ItemStatus.FULFILLED);
                    orderEventResult.addUpdate(new OrderUpdate(orderUp.orderId, orderUp.itemId, orderUp.name, orderUp.item, OrderStatus.FULFILLED, orderUp.madeBy));
                }
            });
        }

        if (this.getBaristaLineItems().isPresent() && this.getKitchenLineItems().isPresent()) {
            if(Stream.concat(this.baristaLineItems.stream(), this.kitchenLineItems.stream())
                    .allMatch(lineItem -> lineItem.getItemStatus().equals(ItemStatus.FULFILLED))){
                this.setOrderStatus(OrderStatus.FULFILLED);
            }
        }else if (this.getBaristaLineItems().isPresent()) {
            if(this.baristaLineItems.stream()
                    .allMatch(lineItem -> lineItem.getItemStatus().equals(ItemStatus.FULFILLED))){
                this.setOrderStatus(OrderStatus.FULFILLED);
            }
        }else if (this.getKitchenLineItems().isPresent()) {
            if(this.kitchenLineItems.stream()
                    .allMatch(lineItem -> lineItem.getItemStatus().equals(ItemStatus.FULFILLED))){
                this.setOrderStatus(OrderStatus.FULFILLED);
            }
        }

        orderEventResult.setOrder(this);
        return orderEventResult;
    }

    public void addBaristaLineItem(LineItem lineItem) {
        if (this.baristaLineItems == null) { this.baristaLineItems = new ArrayList<>(); }
        lineItem.setOrder(this);
        lineItem.setLineItemType("BARISTA");
        this.baristaLineItems.add(lineItem);
    }

    public void addKitchenLineItem(LineItem lineItem) {
        if (this.kitchenLineItems == null) { this.kitchenLineItems = new ArrayList<>(); }
        lineItem.setOrder(this);
        lineItem.setLineItemType("KITCHEN");
        this.kitchenLineItems.add(lineItem);
    }

    public Optional<String> getLoyaltyMemberId() { return Optional.ofNullable(loyaltyMemberId); }
    public Optional<List<LineItem>> getBaristaLineItems() { return Optional.ofNullable(baristaLineItems); }
    public Optional<List<LineItem>> getKitchenLineItems() { return Optional.ofNullable(kitchenLineItems); }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Order{");
        sb.append("orderId='").append(orderId).append('\'');
        sb.append(", orderSource=").append(orderSource);
        sb.append(", loyaltyMemberId='").append(loyaltyMemberId).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", orderStatus=").append(orderStatus);
        sb.append(", location=").append(location);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId != null ? orderId.equals(order.orderId) : order.orderId == null;
    }

    @Override
    public int hashCode() {
        return orderId != null ? orderId.hashCode() : 0;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public OrderSource getOrderSource() { return orderSource; }
    public void setOrderSource(OrderSource orderSource) { this.orderSource = orderSource; }
    public void setLoyaltyMemberId(String loyaltyMemberId) { this.loyaltyMemberId = loyaltyMemberId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public void setBaristaLineItems(List<LineItem> baristaLineLineItems) { this.baristaLineItems = baristaLineLineItems; }
    public void setKitchenLineItems(List<LineItem> kitchenLineLineItems) { this.kitchenLineItems = kitchenLineLineItems; }
}
