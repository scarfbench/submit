package org.woehlke.jakartaee.petclinic.visit.db;

import org.woehlke.jakartaee.petclinic.pet.db.Pet;

import java.util.List;

public interface VisitService {

    List<Visit> getAll();

    Visit findById(long id);

    Visit addNew(Visit visit);

    Visit update(Visit visit);

    void delete(long id);

    List<Visit> getVisits(Pet pet);
}
