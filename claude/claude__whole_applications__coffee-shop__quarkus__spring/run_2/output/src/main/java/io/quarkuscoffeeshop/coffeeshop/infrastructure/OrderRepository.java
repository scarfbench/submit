package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByOrderId(String orderId);
}
