package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

@Repository
@Transactional
public class JpaCargoRepository implements CargoRepository {

  private static final Logger logger = Logger.getLogger(JpaCargoRepository.class.getName());

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Override
  public Cargo find(TrackingId trackingId) {
    Cargo cargo;
    try {
      cargo = entityManager
          .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
          .setParameter("trackingId", trackingId)
          .getSingleResult();
    } catch (NoResultException e) {
      logger.log(Level.FINE, "Find called on non-existant tracking ID.", e);
      cargo = null;
    }
    return cargo;
  }

  @Override
  public List<Cargo> findAll() {
    return entityManager.createNamedQuery("Cargo.findAll", Cargo.class).getResultList();
  }

  @Override
  public void store(Cargo cargo) {
    cargo.getItinerary().getLegs().forEach(leg -> entityManager.persist(leg));
    entityManager.persist(cargo);
    eventPublisher.publishEvent(new CargoUpdatedEvent(cargo));
  }

  @Override
  public TrackingId nextTrackingId() {
    String random = UUID.randomUUID().toString().toUpperCase();
    return new TrackingId(random.substring(0, random.indexOf("-")));
  }
}
