package org.woehlke.jakartaee.petclinic.owner.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.owner.Owner;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.io.Serializable;
import java.util.*;

@Log
@ApplicationScoped
public class OwnerDaoImpl implements OwnerDao, Serializable {

    private static final long serialVersionUID = 1313423542L;

    @Inject
    EntityManager entityManager;

    @Override
    public List<Owner> getAll() {
        TypedQuery<Owner> q = entityManager.createNamedQuery("Owner.getAll", Owner.class);
        return q.getResultList();
    }

    @Override
    @Transactional
    public void delete(long id) {
        Owner owner = entityManager.find(Owner.class, id);
        log.info("delete Owner: " + owner.toString());
        entityManager.remove(owner);
    }

    @Override
    @Transactional
    public Owner addNew(Owner owner) {
        owner.setUuid(UUID.randomUUID());
        log.info("addNew Owner: " + owner.toString());
        entityManager.persist(owner);
        return owner;
    }

    @Override
    public Owner findById(long id) {
        return entityManager.find(Owner.class, id);
    }

    @Override
    @Transactional
    public Owner update(Owner owner) {
        log.info("update Owner: " + owner.toString());
        return entityManager.merge(owner);
    }

    @Override
    public List<Owner> search(String searchterm) {
        log.info("search Owner: " + searchterm);
        TypedQuery<Owner> q = entityManager.createNamedQuery("Owner.search", Owner.class);
        q.setParameter("searchterm", "%" + searchterm + "%");
        List<Owner> list = q.getResultList();
        return list;
    }

    @Override
    public void resetSearchIndex() {
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: " + OwnerDaoImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: " + OwnerDaoImpl.class.getSimpleName());
    }
}
