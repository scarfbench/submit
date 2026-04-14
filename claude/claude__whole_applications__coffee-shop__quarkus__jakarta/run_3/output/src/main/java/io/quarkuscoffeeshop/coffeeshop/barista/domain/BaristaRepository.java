package io.quarkuscoffeeshop.coffeeshop.barista.domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class BaristaRepository {

    @PersistenceContext(unitName = "coffeeshop")
    EntityManager em;

    public void persist(BaristaItem item) {
        em.persist(item);
    }
}
