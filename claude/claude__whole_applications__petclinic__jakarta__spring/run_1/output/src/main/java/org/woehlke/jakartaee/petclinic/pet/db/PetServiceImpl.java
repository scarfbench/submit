package org.woehlke.jakartaee.petclinic.pet.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.woehlke.jakartaee.petclinic.visit.Visit;
import org.woehlke.jakartaee.petclinic.visit.db.VisitRepository;

import java.util.List;
import java.util.UUID;


/**
 *
 */
@Log
@Service
@Transactional
public class PetServiceImpl implements PetService  {

    private static final long serialVersionUID = -2093524918552358722L;

    private final PetRepository petRepository;
    private final VisitRepository visitRepository;

    @Autowired
    public PetServiceImpl(PetRepository petRepository, VisitRepository visitRepository) {
        this.petRepository = petRepository;
        this.visitRepository = visitRepository;
    }

    @Override
    public Pet addNew(Pet pet) {
        pet.setUuid(UUID.randomUUID());
        log.info("addNew Pet: " + pet.toString());
        return this.petRepository.save(pet);
    }

    @Override
    public List<Pet> getAll() {
        return this.petRepository.findAllOrderByName();
    }

    @Override
    public Pet findById(long petId) {
        return this.petRepository.findById(petId).orElse(null);
    }

    @Override
    public Pet update(Pet pet) {
        log.info("update Pet: " + pet.toString());
        return this.petRepository.save(pet);
    }

    @Override
    public void delete(long id) {
        log.info("delete Pet: " + id);
        this.petRepository.deleteById(id);
    }


    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: "+PetServiceImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: "+PetServiceImpl.class.getSimpleName());
    }

    @Override
    public List<Visit> getVisits(Pet pet) {
        return this.visitRepository.findByPet(pet);
    }

    @Override
    public List<Pet> getAllPetsOfAnOwner(Owner owner) {
        return this.petRepository.findByOwner(owner);
    }
}
