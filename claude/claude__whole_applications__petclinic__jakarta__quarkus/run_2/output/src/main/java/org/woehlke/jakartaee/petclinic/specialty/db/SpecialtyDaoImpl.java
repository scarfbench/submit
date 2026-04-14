package org.woehlke.jakartaee.petclinic.specialty.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.TypedQuery;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Log
@ApplicationScoped
public class SpecialtyDaoImpl implements SpecialtyDao, Serializable {

    private static final long serialVersionUID = 1355422039564914705L;

    @Inject
    EntityManager entityManager;

    @Override
    public List<Specialty> getAll() {
        TypedQuery<Specialty> q = entityManager.createNamedQuery("Specialty.getAll", Specialty.class);
        return q.getResultList();
    }

    @Override
    public Specialty findById(long id) {
        Specialty specialty = entityManager.find(Specialty.class, id);
        return specialty;
    }

    @Override
    public Specialty findSpecialtyByName(String name) {
        TypedQuery<Specialty> query = entityManager.createNamedQuery(
                "Specialty.findSpecialtyByName", Specialty.class
        );
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public void delete(long id) {
        Specialty specialty = entityManager.find(Specialty.class, id);
        entityManager.remove(specialty);
    }

    @Override
    @Transactional
    public Specialty addNew(Specialty specialty) {
        specialty.setUuid(UUID.randomUUID());
        specialty = updateSearchindex(specialty);
        log.info("addNewSpecialty: " + specialty.toString());
        entityManager.persist(specialty);
        log.info("persisted: " + specialty.toString());
        return specialty;
    }

    @Override
    @Transactional
    public Specialty update(Specialty specialty) {
        specialty = updateSearchindex(specialty);
        log.info("update: " + specialty.toString());
        specialty = entityManager.merge(specialty);
        log.info("merged: " + specialty.toString());
        return specialty;
    }

    private Specialty updateSearchindex(Specialty specialty) {
        String element[] = specialty.getName().split("\\W");
        StringBuilder b = new StringBuilder();
        for (String e : element) {
            b.append(e);
            b.append(" ");
        }
        specialty.setSearchindex(b.toString());
        return specialty;
    }

    @Override
    public List<Specialty> search(String searchterm) {
        log.info("search Specialty for: " + searchterm);
        TypedQuery<Specialty> q = entityManager.createNamedQuery("Specialty.search", Specialty.class);
        q.setParameter("searchterm", "%" + searchterm + "%");
        List<Specialty> list = q.getResultList();
        return list;
    }

    @Override
    public void resetSearchIndex() {
        log.info("resetSearchIndex Specialty ");
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: " + SpecialtyDaoImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: " + SpecialtyDaoImpl.class.getSimpleName());
    }
}
