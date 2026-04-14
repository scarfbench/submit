package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.io.Serializable;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;

@ApplicationScoped
public class VoyageRepositoryImpl implements VoyageRepository, Serializable {

  private static final long serialVersionUID = 1L;

  @PersistenceContext private EntityManager entityManager;

  @Override
  public Voyage find(VoyageNumber voyageNumber) {
    try {
      return entityManager
          .createQuery("SELECT v FROM Voyage v WHERE v.voyageNumber = :vn", Voyage.class)
          .setParameter("vn", voyageNumber)
          .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<Voyage> findAll() {
    return entityManager.createQuery("SELECT v FROM Voyage v", Voyage.class).getResultList();
  }

  @Override
  public void store(Voyage voyage) {
    entityManager.merge(voyage);
  }
}
