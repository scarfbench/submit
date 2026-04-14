package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.io.Serializable;
import java.util.List;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional(readOnly = true)
public class JpaVoyageRepository implements VoyageRepository, Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Voyage find(VoyageNumber voyageNumber) {
        return entityManager
            .createNamedQuery("Voyage.findByVoyageNumber", Voyage.class)
            .setParameter("voyageNumber", voyageNumber)
            .getSingleResult();
    }

    @Override
    public List<Voyage> findAll() {
        return entityManager.createQuery("SELECT v FROM Voyage v", Voyage.class).getResultList();
    }
}
