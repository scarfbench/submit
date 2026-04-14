package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import io.quarkuscoffeeshop.coffeeshop.domain.Order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class OrderRepository {

    @PersistenceContext
    EntityManager em;

    public Order findById(final String id) {
        return em.find(Order.class, id);
    }

    public void persist(Order order) {
        em.persist(order);
    }

    public void persistAndFlush(Order order) {
        em.persist(order);
        em.flush();
    }
}
