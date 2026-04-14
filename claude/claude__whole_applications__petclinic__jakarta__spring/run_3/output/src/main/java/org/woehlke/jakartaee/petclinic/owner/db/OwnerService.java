package org.woehlke.jakartaee.petclinic.owner.db;

import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.util.List;

public interface OwnerService {

    List<Owner> getAll();
    Owner findById(long id);
    Owner addNew(Owner owner);
    Owner update(Owner owner);
    void delete(long id);
    List<Owner> search(String searchterm);
    List<Pet> getPetsAsList(Owner owner);
    Visit addNewVisit(Visit visit);
}
