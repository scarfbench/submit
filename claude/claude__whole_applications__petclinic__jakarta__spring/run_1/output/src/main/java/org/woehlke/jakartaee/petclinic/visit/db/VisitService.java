package org.woehlke.jakartaee.petclinic.visit.db;

import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.util.List;

/**
 *
 */
public interface VisitService {

    long serialVersionUID = -207047254562666324L;

    List<Visit> getAll();

    Visit findById(long id);

    Visit addNew(Visit visit);

    Visit update(Visit visit);

    void delete(long id);

    List<Visit> getAllVisitsOfAnPet(Pet pet);
}
