package io.quarkuscoffeeshop.coffeeshop.kitchen.domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class KitchenOrderRepository {

    @PersistenceContext(unitName = "kitchen")
    EntityManager entityManager;

    public void persist(KitchenOrder order) {
        entityManager.persist(order);
    }
}
