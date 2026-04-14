package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.io.Serializable;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;

@ApplicationScoped
public class JpaVoyageRepository implements VoyageRepository, Serializable {

  private static final long serialVersionUID = 1L;

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Voyage find(VoyageNumber voyageNumber) {
    return entityManager.createNamedQuery("Voyage.findByVoyageNumber", Voyage.class)
        .setParameter("voyageNumber", voyageNumber)
        .getSingleResult();
  }

  public List<Voyage> listAll() {
    return entityManager.createQuery("SELECT v FROM Voyage v", Voyage.class).getResultList();
  }

  public List<Voyage> listAllSorted() {
    return entityManager.createQuery("SELECT v FROM Voyage v ORDER BY v.voyageNumber", Voyage.class).getResultList();
  }
}
