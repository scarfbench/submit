package com.coffeeshop.orders.api;

import com.coffeeshop.common.domain.OrderAck;
import com.coffeeshop.common.domain.OrderRequest;
import com.coffeeshop.orders.domain.OrderEntity;
import com.coffeeshop.orders.domain.OrderRepository;
import com.coffeeshop.orders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<OrderAck> place(@Valid @RequestBody OrderRequest req) {
        String id = orderService.place(req);
        return ResponseEntity.accepted().body(new OrderAck(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderEntity> status(@PathVariable("id") long id) {
        return repo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
