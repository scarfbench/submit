package org.woehlke.jakartaee.petclinic.pet.db;

import org.woehlke.jakartaee.petclinic.application.framework.db.CrudService;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.util.List;

public interface PetService extends CrudService<Pet> {
    List<Visit> getVisits(Pet pet);
    List<Pet> getAllPetsOfAnOwner(Owner entity);
}
