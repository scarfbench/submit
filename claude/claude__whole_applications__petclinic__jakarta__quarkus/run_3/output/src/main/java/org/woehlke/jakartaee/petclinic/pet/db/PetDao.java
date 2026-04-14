package org.woehlke.jakartaee.petclinic.pet.db;

import org.woehlke.jakartaee.petclinic.application.framework.db.CrudDao;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.util.List;

public interface PetDao extends CrudDao<Pet> {
    List<Pet> getPetsAsList(Owner owner);
    List<Visit> getVisits(Pet pet);
}
