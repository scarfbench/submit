package io.quarkuscoffeeshop.coffeeshop.kitchen.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KitchenOrderRepository extends JpaRepository<KitchenOrder, Long> {
}
