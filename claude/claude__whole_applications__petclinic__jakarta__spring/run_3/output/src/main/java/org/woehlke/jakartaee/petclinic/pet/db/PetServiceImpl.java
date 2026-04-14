package org.woehlke.jakartaee.petclinic.pet.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;
import org.woehlke.jakartaee.petclinic.visit.db.VisitRepository;

import java.util.List;
import java.util.UUID;

@Log
@Service
@Transactional
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final VisitRepository visitRepository;

    @Autowired
    public PetServiceImpl(PetRepository petRepository, VisitRepository visitRepository) {
        this.petRepository = petRepository;
        this.visitRepository = visitRepository;
    }

    @Override
    public List<Pet> getAll() {
        return petRepository.findAllByOrderByBirthDateAscNameAsc();
    }

    @Override
    public Pet findById(long id) {
        return petRepository.findById(id).orElse(null);
    }

    @Override
    public Pet addNew(Pet pet) {
        pet.setUuid(UUID.randomUUID());
        log.info("addNew Pet: " + pet.toString());
        return petRepository.save(pet);
    }

    @Override
    public Pet update(Pet pet) {
        log.info("update Pet: " + pet.toString());
        return petRepository.save(pet);
    }

    @Override
    public void delete(long id) {
        petRepository.deleteById(id);
    }

    @Override
    public List<Visit> getVisits(Pet pet) {
        return visitRepository.findByPetOrderByDateAsc(pet);
    }

    @Override
    public List<Pet> getAllPetsOfAnOwner(Owner owner) {
        return petRepository.findByOwnerOrderByNameAsc(owner);
    }
}
