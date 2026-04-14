package org.woehlke.jakartaee.petclinic.pettype.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pettype.PetType;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.TypedQuery;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Log
@ApplicationScoped
public class PetTypeDaoImpl implements PetTypeDao, Serializable {

    private static final long serialVersionUID = -7332614951852278897L;

    @Inject
    EntityManager entityManager;

    @Override
    public List<PetType> getAll() {
        TypedQuery<PetType> q = entityManager.createNamedQuery("PetType.getAll", PetType.class);
        return q.getResultList();
    }

    @Override
    @Transactional
    public void delete(long id) {
        PetType petType = entityManager.find(PetType.class, id);
        log.info("delete PetType: " + petType.toString());
        entityManager.remove(petType);
    }

    @Override
    @Transactional
    public PetType addNew(PetType petType) {
        log.info("addNew PetType: " + petType.toString());
        petType.setUuid(UUID.randomUUID());
        if (petType.getId() != null) {
            throw new IllegalArgumentException("new PetType Entity must not have a PetType.id before persisting");
        }
        entityManager.persist(petType);
        return petType;
    }

    @Override
    public PetType findById(long id) {
        return entityManager.find(PetType.class, id);
    }

    @Override
    @Transactional
    public PetType update(PetType petType) {
        log.info("update PetType: " + petType.toString());
        if (petType.getId() == null) {
            throw new IllegalArgumentException("new PetType Entity must have a PetType.id before merging");
        }
        return entityManager.merge(petType);
    }

    @Override
    public List<PetType> search(String searchterm) {
        log.info("search PetType for: " + searchterm);
        TypedQuery<PetType> q = entityManager.createNamedQuery("PetType.search", PetType.class);
        q.setParameter("searchterm", "%" + searchterm + "%");
        List<PetType> list = q.getResultList();
        return list;
    }

    @Override
    public void resetSearchIndex() {
    }

    @Override
    public PetType findByName(String name) {
        TypedQuery<PetType> query = entityManager.createNamedQuery("PetType.findByName", PetType.class);
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    @PostConstruct
    public void postConstruct() {
        log.info("PostConstruct: " + PetTypeDaoImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("PreDestroy: " + PetTypeDaoImpl.class.getSimpleName());
    }
}
