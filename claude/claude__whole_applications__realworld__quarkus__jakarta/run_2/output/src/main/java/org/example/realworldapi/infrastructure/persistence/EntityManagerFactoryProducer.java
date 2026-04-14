package org.example.realworldapi.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Dependent
public class EntityManagerFactoryProducer {

  private static volatile EntityManagerFactory emf;

  public static void initialize() {
    if (emf == null) {
      synchronized (EntityManagerFactoryProducer.class) {
        if (emf == null) {
          emf = Persistence.createEntityManagerFactory("realworld");
        }
      }
    }
  }

  @Produces
  @ApplicationScoped
  public EntityManagerFactory createEntityManagerFactory() {
    initialize();
    return emf;
  }
}
