package org.woehlke.jakartaee.petclinic.owner.db;

import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.util.List;

/**
 * Created by tw on 10.03.14.
 */
public interface OwnerService {

    long serialVersionUID = -5744255576144969978L;

    List<Owner> getAll();

    Owner findById(long id);

    Owner addNew(Owner owner);

    Owner update(Owner owner);

    void delete(long id);

    List<Owner> search(String searchterm);

    void resetSearchIndex();

    Visit addNewVisit(Visit visit);

    List<Pet> getPetsAsList(Owner owner);

    String getPetsAsString(Owner owner);

}
