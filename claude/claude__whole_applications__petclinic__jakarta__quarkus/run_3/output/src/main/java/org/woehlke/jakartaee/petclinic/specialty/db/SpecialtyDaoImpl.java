package org.woehlke.jakartaee.petclinic.specialty.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.UUID;

@Log
@ApplicationScoped
public class SpecialtyDaoImpl implements SpecialtyDao {

    @Inject
    EntityManager entityManager;

    @Override
    public List<Specialty> getAll() {
        TypedQuery<Specialty> q = entityManager.createNamedQuery("Specialty.getAll", Specialty.class);
        return q.getResultList();
    }

    @Override
    @Transactional
    public Specialty addNew(Specialty specialty) {
        specialty.setUuid(UUID.randomUUID());
        log.info("addNew Specialty: " + specialty.toString());
        entityManager.persist(specialty);
        return specialty;
    }

    @Override
    public Specialty findById(long id) { return entityManager.find(Specialty.class, id); }

    @Override
    @Transactional
    public Specialty update(Specialty specialty) {
        log.info("update Specialty: " + specialty.toString());
        return entityManager.merge(specialty);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Specialty s = entityManager.find(Specialty.class, id);
        log.info("delete Specialty: " + s.toString());
        entityManager.remove(s);
    }

    @Override
    public Specialty findSpecialtyByName(String name) {
        TypedQuery<Specialty> q = entityManager.createNamedQuery("Specialty.findSpecialtyByName", Specialty.class);
        q.setParameter("name", name);
        List<Specialty> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Specialty> search(String searchterm) {
        TypedQuery<Specialty> q = entityManager.createNamedQuery("Specialty.search", Specialty.class);
        q.setParameter("searchterm", "%" + searchterm + "%");
        return q.getResultList();
    }

    @Override
    public void resetSearchIndex() {}

    @PostConstruct
    public void postConstruct() { log.info("postConstruct: " + SpecialtyDaoImpl.class.getSimpleName()); }

    @PreDestroy
    public void preDestroy() { log.info("preDestroy: " + SpecialtyDaoImpl.class.getSimpleName()); }
}
