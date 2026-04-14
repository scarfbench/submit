package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.infrastructure.events.cdi.CargoUpdated;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCargoRepository implements CargoRepository, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(JpaCargoRepository.class.getName());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Cargo find(TrackingId trackingId) {
        Cargo cargo;
        try {
            cargo = entityManager.createNamedQuery("Cargo.findByTrackingId", Cargo.class)
                    .setParameter("trackingId", trackingId)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.log(Level.FINE, "Find called on non-existant tracking ID.", e);
            cargo = null;
        }
        return cargo;
    }

    @Override
    public void store(Cargo cargo) {
        cargo.getItinerary().getLegs().forEach(leg -> {
            if (!entityManager.contains(leg)) {
                entityManager.persist(leg);
            }
        });
        if (entityManager.contains(cargo)) {
            entityManager.merge(cargo);
        } else {
            entityManager.persist(cargo);
        }
        entityManager.flush();
        eventPublisher.publishEvent(new CargoUpdated(this, cargo));
    }

    @Override
    public TrackingId nextTrackingId() {
        String random = UUID.randomUUID().toString().toUpperCase();
        return new TrackingId(random.substring(0, random.indexOf("-")));
    }

    @Override
    public List<Cargo> findAll() {
        return entityManager.createQuery("Select c from Cargo c", Cargo.class).getResultList();
    }
}
