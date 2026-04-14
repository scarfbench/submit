package com.coffeeshop.orders.service;

import com.coffeeshop.common.domain.OrderRequest;
import com.coffeeshop.orders.domain.OrderEntity;
import com.coffeeshop.orders.domain.OrderRepository;
import com.coffeeshop.orders.messaging.OrdersPipeline;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository repo;
    private final OrdersPipeline pipeline;

    public OrderService(OrderRepository repo, OrdersPipeline pipeline) {
        this.repo = repo;
        this.pipeline = pipeline;
    }

    @Transactional
    public String place(@Valid OrderRequest req) {
        var e = new OrderEntity();
        e.setCustomer(req.customer());
        e.setItem(req.item());
        e.setQuantity(req.quantity());
        repo.save(e);

        // simple routing: drinks -> barista, everything else -> kitchen
        if (isDrink(req.item())) {
            pipeline.publishToBarista(new OrdersPipeline.OrderCommand("BARISTA", e.getId(), req.item(), req.quantity()));
        } else {
            pipeline.publishToKitchen(new OrdersPipeline.OrderCommand("KITCHEN", e.getId(), req.item(), req.quantity()));
        }
        return String.valueOf(e.getId());
    }

    private boolean isDrink(String item) {
        if (item == null) return false;
        var s = item.toLowerCase();
        return s.contains("coffee") || s.contains("latte") || s.contains("espresso")
            || s.contains("cappuccino") || s.contains("americano") || s.contains("mocha")
            || s.contains("tea");
    }
}
