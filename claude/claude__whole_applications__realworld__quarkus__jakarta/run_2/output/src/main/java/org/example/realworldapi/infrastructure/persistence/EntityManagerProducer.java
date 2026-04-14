package org.example.realworldapi.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@ApplicationScoped
public class EntityManagerProducer {

  @Produces
  @RequestScoped
  public EntityManager createEntityManager(EntityManagerFactory emf) {
    return emf.createEntityManager();
  }

  public void closeEntityManager(@Disposes EntityManager em) {
    if (em.isOpen()) {
      em.close();
    }
  }
}
