package io.quarkuscoffeeshop.coffeeshop.kitchen.domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class KitchenOrderRepository {

    @PersistenceContext
    EntityManager em;

    public void persist(KitchenOrder kitchenOrder) {
        em.persist(kitchenOrder);
    }
}
