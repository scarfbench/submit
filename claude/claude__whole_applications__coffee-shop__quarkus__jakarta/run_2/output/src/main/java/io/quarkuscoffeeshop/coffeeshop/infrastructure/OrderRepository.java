package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import io.quarkuscoffeeshop.coffeeshop.domain.Order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class OrderRepository {

    @PersistenceContext(unitName = "coffeeshop")
    EntityManager entityManager;

    public void persist(Order order) {
        entityManager.persist(order);
    }

    public void persistAndFlush(Order order) {
        entityManager.persist(order);
        entityManager.flush();
    }

    public Order findById(String id) {
        return entityManager.find(Order.class, id);
    }
}
