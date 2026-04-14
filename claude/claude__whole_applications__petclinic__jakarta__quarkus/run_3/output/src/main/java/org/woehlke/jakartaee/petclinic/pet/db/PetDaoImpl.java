package org.woehlke.jakartaee.petclinic.pet.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.UUID;

@Log
@ApplicationScoped
public class PetDaoImpl implements PetDao {

    @Inject
    EntityManager entityManager;

    @Override
    public List<Pet> getPetsAsList(Owner owner) {
        TypedQuery<Pet> q = entityManager.createNamedQuery("Pet.getPetsAsList", Pet.class);
        q.setParameter("owner", owner);
        return q.getResultList();
    }

    @Override
    public List<Visit> getVisits(Pet pet) {
        TypedQuery<Visit> q = entityManager.createNamedQuery("Visit.getVisits", Visit.class);
        q.setParameter("pet", pet);
        return q.getResultList();
    }

    @Override
    @Transactional
    public Pet addNew(Pet pet) {
        pet.setUuid(UUID.randomUUID());
        log.info("transient New Pet: " + pet.toString());
        entityManager.persist(pet);
        log.info("persistent New Pet: " + pet.toString());
        return pet;
    }

    @Override
    public List<Pet> getAll() {
        TypedQuery<Pet> q = entityManager.createNamedQuery("Pet.getAll", Pet.class);
        return q.getResultList();
    }

    @Override
    public Pet findById(long petId) { return entityManager.find(Pet.class, petId); }

    @Override
    @Transactional
    public Pet update(Pet pet) {
        log.info("update Pet: " + pet.toString());
        return entityManager.merge(pet);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Pet p = this.findById(id);
        log.info("delete Pet: " + p.toString());
        entityManager.remove(p);
    }

    @PostConstruct
    public void postConstruct() { log.info("postConstruct: " + PetDaoImpl.class.getSimpleName()); }

    @PreDestroy
    public void preDestroy() { log.info("preDestroy: " + PetDaoImpl.class.getSimpleName()); }
}
