package org.petclinic.repository;

import java.util.List;

import org.petclinic.model.Vet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@ApplicationScoped
public class VetRepository {

    @PersistenceContext(unitName = "petclinicPU")
    private EntityManager em;

    public List<Vet> findAll() {
        TypedQuery<Vet> query = em.createQuery("SELECT v FROM Vet v ORDER BY v.lastName", Vet.class);
        return query.getResultList();
    }

    public List<Vet> findAll(int page, int pageSize) {
        TypedQuery<Vet> query = em.createQuery("SELECT v FROM Vet v ORDER BY v.lastName", Vet.class);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    public long count() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(v) FROM Vet v", Long.class);
        return query.getSingleResult();
    }
}
