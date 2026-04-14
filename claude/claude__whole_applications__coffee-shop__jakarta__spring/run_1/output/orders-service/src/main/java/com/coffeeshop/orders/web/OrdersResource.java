package com.coffeeshop.orders.web;

import com.coffeeshop.common.domain.OrderRequest;
import com.coffeeshop.orders.domain.OrderEntity;
import com.coffeeshop.orders.domain.OrderRepository;
import com.coffeeshop.orders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrdersResource {

    private final OrderService orderService;
    private final OrderRepository repo;

    public OrdersResource(OrderService orderService, OrderRepository repo) {
        this.orderService = orderService;
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OrderRequest request) {
        String id = orderService.place(request);
        return ResponseEntity.accepted().body(Map.of("id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable long id) {
        OrderEntity entity = repo.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entity);
    }
}
