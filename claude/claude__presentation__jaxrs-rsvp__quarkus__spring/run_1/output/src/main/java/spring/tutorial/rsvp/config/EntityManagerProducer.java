package spring.tutorial.rsvp.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

/**
 * Provides EntityManager for CDI-style injection
 */
@Component
public class EntityManagerProducer {

    @PersistenceContext
    private EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
