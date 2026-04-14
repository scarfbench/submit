package io.quarkuscoffeeshop.coffeeshop.kitchen.domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class KitchenOrderRepository {

    @PersistenceContext(unitName = "coffeeshop")
    EntityManager em;

    public void persist(KitchenOrder order) {
        em.persist(order);
    }
}
