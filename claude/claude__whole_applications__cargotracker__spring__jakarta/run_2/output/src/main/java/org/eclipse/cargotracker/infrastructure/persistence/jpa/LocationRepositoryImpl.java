package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.io.Serializable;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.UnLocode;

@ApplicationScoped
public class LocationRepositoryImpl implements LocationRepository, Serializable {

  private static final long serialVersionUID = 1L;

  @PersistenceContext private EntityManager entityManager;

  @Override
  public Location find(UnLocode unLocode) {
    try {
      return entityManager
          .createQuery("SELECT l FROM Location l WHERE l.unLocode = :code", Location.class)
          .setParameter("code", unLocode)
          .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<Location> findAll() {
    return entityManager.createQuery("SELECT l FROM Location l", Location.class).getResultList();
  }

  @Override
  public void store(Location location) {
    entityManager.merge(location);
  }
}
