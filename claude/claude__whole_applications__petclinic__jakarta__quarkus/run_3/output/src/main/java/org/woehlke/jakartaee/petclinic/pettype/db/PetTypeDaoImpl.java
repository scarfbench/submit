package org.woehlke.jakartaee.petclinic.pettype.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pettype.PetType;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.UUID;

@Log
@ApplicationScoped
public class PetTypeDaoImpl implements PetTypeDao {

    @Inject
    EntityManager entityManager;

    @Override
    public List<PetType> getAll() {
        TypedQuery<PetType> q = entityManager.createNamedQuery("PetType.getAll", PetType.class);
        return q.getResultList();
    }

    @Override
    @Transactional
    public PetType addNew(PetType petType) {
        petType.setUuid(UUID.randomUUID());
        log.info("addNew PetType: " + petType.toString());
        entityManager.persist(petType);
        return petType;
    }

    @Override
    public PetType findById(long id) { return entityManager.find(PetType.class, id); }

    @Override
    @Transactional
    public PetType update(PetType petType) {
        log.info("update PetType: " + petType.toString());
        return entityManager.merge(petType);
    }

    @Override
    @Transactional
    public void delete(long id) {
        PetType p = entityManager.find(PetType.class, id);
        log.info("delete PetType: " + p.toString());
        entityManager.remove(p);
    }

    @Override
    public PetType findByName(String name) {
        TypedQuery<PetType> q = entityManager.createNamedQuery("PetType.findByName", PetType.class);
        q.setParameter("name", name);
        List<PetType> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<PetType> search(String searchterm) {
        TypedQuery<PetType> q = entityManager.createNamedQuery("PetType.search", PetType.class);
        q.setParameter("searchterm", "%" + searchterm + "%");
        return q.getResultList();
    }

    @Override
    public void resetSearchIndex() {}

    @PostConstruct
    public void postConstruct() { log.info("postConstruct: " + PetTypeDaoImpl.class.getSimpleName()); }

    @PreDestroy
    public void preDestroy() { log.info("preDestroy: " + PetTypeDaoImpl.class.getSimpleName()); }
}
