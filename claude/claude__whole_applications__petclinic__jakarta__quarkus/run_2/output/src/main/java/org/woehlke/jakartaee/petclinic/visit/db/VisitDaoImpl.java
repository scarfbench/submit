package org.woehlke.jakartaee.petclinic.visit.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Log
@ApplicationScoped
public class VisitDaoImpl implements VisitDao, Serializable {

    private static final long serialVersionUID = 892248114140040519L;

    @Inject
    EntityManager entityManager;

    @Override
    public List<Visit> getVisits(@NotNull Pet pet) {
        TypedQuery<Visit> q = entityManager.createNamedQuery("Visit.getVisits", Visit.class);
        q.setParameter("pet", pet);
        List<Visit> list = q.getResultList();
        return list;
    }

    @Override
    public List<Visit> getAll() {
        TypedQuery<Visit> q = entityManager.createNamedQuery("Visit.getAll", Visit.class);
        List<Visit> list = q.getResultList();
        return list;
    }

    @Override
    public Visit findById(long id) {
        return entityManager.find(Visit.class, id);
    }

    @Override
    @Transactional
    public Visit addNew(Visit visit) {
        visit.setUuid(UUID.randomUUID());
        log.info("addNew Visit: " + visit.toString());
        entityManager.persist(visit);
        return visit;
    }

    @Override
    @Transactional
    public Visit update(Visit visit) {
        log.info("update Visit: " + visit.toString());
        return entityManager.merge(visit);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Visit visit = entityManager.find(Visit.class, id);
        log.info("delete Visit: " + visit.toString());
        entityManager.remove(visit);
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: " + VisitDaoImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: " + VisitDaoImpl.class.getSimpleName());
    }
}
