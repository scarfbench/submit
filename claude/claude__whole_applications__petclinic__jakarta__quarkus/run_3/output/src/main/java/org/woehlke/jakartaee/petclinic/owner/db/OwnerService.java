package org.woehlke.jakartaee.petclinic.owner.db;

import org.woehlke.jakartaee.petclinic.application.framework.db.CrudService;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.util.List;

public interface OwnerService extends CrudService<Owner> {
    List<Owner> search(String searchterm);
    List<Pet> getPetsAsList(Owner owner);
    String getPetsAsString(Owner owner);
    Visit addNewVisit(Visit visit);
    void resetSearchIndex();
}
