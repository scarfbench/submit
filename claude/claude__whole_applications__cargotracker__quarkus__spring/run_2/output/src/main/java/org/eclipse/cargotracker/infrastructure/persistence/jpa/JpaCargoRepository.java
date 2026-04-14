package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional
public class JpaCargoRepository implements CargoRepository, Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private Logger logger;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Cargo find(TrackingId trackingId) {
        try {
            return entityManager
                .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
                .setParameter("trackingId", trackingId)
                .getSingleResult();
        } catch (NoResultException e) {
            logger.log(Level.FINE, "Find called on non-existant tracking ID.", e);
            return null;
        }
    }

    @Override
    public List<Cargo> findAll() {
        return entityManager.createQuery("SELECT c FROM Cargo c", Cargo.class).getResultList();
    }

    @Override
    public void store(Cargo cargo) {
        cargo.getItinerary().getLegs().forEach(leg -> entityManager.persist(leg));
        entityManager.persist(cargo);
        eventPublisher.publishEvent(cargo);
    }

    @Override
    public TrackingId nextTrackingId() {
        String random = UUID.randomUUID().toString().toUpperCase();
        return new TrackingId(random.substring(0, random.indexOf("-")));
    }
}
