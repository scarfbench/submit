package io.quarkuscoffeeshop.coffeeshop.barista.domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class BaristaRepository {

    @PersistenceContext
    EntityManager em;

    public void persist(BaristaItem baristaItem) {
        em.persist(baristaItem);
    }
}
