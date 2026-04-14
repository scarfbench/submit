package com.coffeeshop.orders.service;

import com.coffeeshop.common.domain.OrderRequest;
import com.coffeeshop.orders.domain.OrderEntity;
import com.coffeeshop.orders.domain.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderService {
    @Inject
    OrderRepository repo;

    /**
     * Persist the order and return the entity with generated ID.
     * Messaging is handled separately by the caller.
     */
    @Transactional
    public OrderEntity place(OrderRequest req) {
        var e = new OrderEntity();
        e.setCustomer(req.customer());
        e.setItem(req.item());
        e.setQuantity(req.quantity());
        repo.save(e);
        return e;
    }

    public boolean isDrink(String item) {
        if (item == null) return false;
        var s = item.toLowerCase();
        return s.contains("coffee") || s.contains("latte") || s.contains("espresso")
            || s.contains("cappuccino") || s.contains("americano") || s.contains("mocha")
            || s.contains("tea");
    }
}
