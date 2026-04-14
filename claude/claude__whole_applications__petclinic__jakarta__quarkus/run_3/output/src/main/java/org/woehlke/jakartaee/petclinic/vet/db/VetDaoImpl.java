package org.woehlke.jakartaee.petclinic.vet.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.vet.Vet;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.UUID;

@Log
@ApplicationScoped
public class VetDaoImpl implements VetDao {

    @Inject
    EntityManager entityManager;

    @Override
    public List<Vet> getAll() {
        TypedQuery<Vet> q = entityManager.createNamedQuery("Vet.getAll", Vet.class);
        return q.getResultList();
    }

    @Override
    @Transactional
    public Vet addNew(Vet vet) {
        vet.setUuid(UUID.randomUUID());
        log.info("addNew Vet: " + vet.toString());
        entityManager.persist(vet);
        return vet;
    }

    @Override
    public Vet findById(long id) { return entityManager.find(Vet.class, id); }

    @Override
    @Transactional
    public Vet update(Vet vet) {
        log.info("update Vet: " + vet.toString());
        return entityManager.merge(vet);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Vet v = entityManager.find(Vet.class, id);
        log.info("delete Vet: " + v.toString());
        entityManager.remove(v);
    }

    @Override
    public List<Vet> search(String searchterm) {
        TypedQuery<Vet> q = entityManager.createNamedQuery("Vet.search", Vet.class);
        q.setParameter("searchterm", "%" + searchterm + "%");
        return q.getResultList();
    }

    @Override
    public void resetSearchIndex() {}

    @PostConstruct
    public void postConstruct() { log.info("postConstruct: " + VetDaoImpl.class.getSimpleName()); }

    @PreDestroy
    public void preDestroy() { log.info("preDestroy: " + VetDaoImpl.class.getSimpleName()); }
}
