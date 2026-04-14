package org.woehlke.jakartaee.petclinic.pet.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.io.Serializable;
import java.util.List;

@Log
@ApplicationScoped
public class PetServiceImpl implements PetService, Serializable {

    private static final long serialVersionUID = -2093524918552358722L;

    @Inject
    PetDao petDao;

    @Override
    @Transactional
    public Pet addNew(Pet pet) {
        log.info("addNew Pet: " + pet.toString());
        return this.petDao.addNew(pet);
    }

    @Override
    public List<Pet> getAll() {
        return this.petDao.getAll();
    }

    @Override
    public Pet findById(long petId) {
        return this.petDao.findById(petId);
    }

    @Override
    @Transactional
    public Pet update(Pet pet) {
        log.info("update Pet: " + pet.toString());
        return this.petDao.update(pet);
    }

    @Override
    @Transactional
    public void delete(long id) {
        log.info("delete Pet: " + id);
        this.petDao.delete(id);
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: " + PetServiceImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: " + PetServiceImpl.class.getSimpleName());
    }

    @Override
    public List<Visit> getVisits(Pet pet) {
        return this.petDao.getVisits(pet);
    }

    @Override
    public List<Pet> getAllPetsOfAnOwner(Owner owner) {
        return this.petDao.getPetsAsList(owner);
    }
}
