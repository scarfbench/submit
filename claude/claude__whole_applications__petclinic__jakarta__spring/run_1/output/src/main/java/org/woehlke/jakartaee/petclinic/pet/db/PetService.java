package org.woehlke.jakartaee.petclinic.pet.db;

import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.util.List;

/**
 *
 */
public interface PetService {

    long serialVersionUID = 7113444329343577727L;

    Pet addNew(Pet pet);

    List<Pet> getAll();

    Pet findById(long petId);

    Pet update(Pet pet);

    void delete(long id);

    List<Visit> getVisits(Pet pet);

    List<Pet> getAllPetsOfAnOwner(Owner entity);

}
