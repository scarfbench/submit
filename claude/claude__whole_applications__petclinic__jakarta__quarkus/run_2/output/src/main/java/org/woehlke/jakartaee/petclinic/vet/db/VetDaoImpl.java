package org.woehlke.jakartaee.petclinic.vet.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.vet.Vet;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.TypedQuery;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Log
@ApplicationScoped
public class VetDaoImpl implements VetDao, Serializable {

    private static final long serialVersionUID = -1003870150408928198L;

    @Inject
    EntityManager entityManager;

    @Override
    public List<Vet> getAll() {
        TypedQuery<Vet> q = entityManager.createNamedQuery("Vet.getAll", Vet.class);
        List<Vet> list = q.getResultList();
        return list;
    }

    @Override
    public Vet findById(long id) {
        Vet vet = entityManager.find(Vet.class, id);
        return vet;
    }

    @Override
    @Transactional
    public void delete(long id) {
        Vet vet = entityManager.find(Vet.class, id);
        log.info("delete Vet: " + vet.toString());
        entityManager.remove(vet);
    }

    @Override
    @Transactional
    public Vet addNew(Vet vet) {
        vet.setUuid(UUID.randomUUID());
        log.info("addNew Vet: " + vet.toString());
        entityManager.persist(vet);
        log.info("added New Vet: " + vet.toString());
        return vet;
    }

    @Override
    @Transactional
    public Vet update(Vet vet) {
        vet.updateSearchindex();
        log.info("update Vet: " + vet.toString());
        return entityManager.merge(vet);
    }

    @Override
    public List<Vet> search(String searchterm) {
        log.info("search Vet: " + searchterm);
        TypedQuery<Vet> q = entityManager.createNamedQuery("Vet.search", Vet.class);
        q.setParameter("searchterm", "%" + searchterm + "%");
        List<Vet> list = q.getResultList();
        return list;
    }

    @Override
    public void resetSearchIndex() {
        log.info("resetSearchIndex Vet");
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: " + VetDaoImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: " + VetDaoImpl.class.getSimpleName());
    }
}
