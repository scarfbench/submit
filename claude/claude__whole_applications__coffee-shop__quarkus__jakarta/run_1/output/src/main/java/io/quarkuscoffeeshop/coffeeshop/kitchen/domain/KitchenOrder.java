package io.quarkuscoffeeshop.coffeeshop.kitchen.domain;

import io.quarkuscoffeeshop.coffeeshop.domain.InstantAttributeConverter;
import io.quarkuscoffeeshop.coffeeshop.domain.Item;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "kitchen_order")
public class KitchenOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String orderId;

    @Enumerated(EnumType.STRING)
    private Item item;

    @Convert(converter = InstantAttributeConverter.class)
    private Instant timeIn;

    @Convert(converter = InstantAttributeConverter.class)
    private Instant timeUp;

    public KitchenOrder() {
    }


    public KitchenOrder(String orderId, Item item, Instant timeIn) {
        this.orderId = orderId;
        this.item = item;
        this.timeIn = timeIn;
    }

    public KitchenOrder(String orderId, Item item, Instant timeIn, Instant timeUp) {
        this.orderId = orderId;
        this.item = item;
        this.timeIn = timeIn;
        this.timeUp = timeUp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("KitchenOrder{");
        sb.append("orderId='").append(orderId).append('\'');
        sb.append(", item=").append(item);
        sb.append(", timeIn=").append(timeIn);
        sb.append(", timeUp=").append(timeUp);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KitchenOrder that = (KitchenOrder) o;

        if (orderId != null ? !orderId.equals(that.orderId) : that.orderId != null) return false;
        if (item != that.item) return false;
        if (timeIn != null ? !timeIn.equals(that.timeIn) : that.timeIn != null) return false;
        return timeUp != null ? timeUp.equals(that.timeUp) : that.timeUp == null;
    }

    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + (timeIn != null ? timeIn.hashCode() : 0);
        result = 31 * result + (timeUp != null ? timeUp.hashCode() : 0);
        return result;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Instant getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(Instant timeIn) {
        this.timeIn = timeIn;
    }

    public Instant getTimeUp() {
        return timeUp;
    }

    public void setTimeUp(Instant timeUp) {
        this.timeUp = timeUp;
    }
}
