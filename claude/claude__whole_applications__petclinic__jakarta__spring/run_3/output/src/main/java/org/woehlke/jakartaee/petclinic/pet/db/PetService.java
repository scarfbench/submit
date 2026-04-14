package org.woehlke.jakartaee.petclinic.pet.db;

import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.util.List;

public interface PetService {

    List<Pet> getAll();
    Pet findById(long id);
    Pet addNew(Pet pet);
    Pet update(Pet pet);
    void delete(long id);
    List<Visit> getVisits(Pet pet);
    List<Pet> getAllPetsOfAnOwner(Owner owner);
}
