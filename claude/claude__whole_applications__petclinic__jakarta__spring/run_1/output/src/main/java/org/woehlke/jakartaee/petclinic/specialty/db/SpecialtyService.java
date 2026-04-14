package org.woehlke.jakartaee.petclinic.specialty.db;

import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import java.util.List;

/**
 *
 */
public interface SpecialtyService {

    long serialVersionUID = -5259594533899166058L;

    List<Specialty> getAll();

    Specialty findById(long id);

    Specialty addNew(Specialty specialty);

    Specialty update(Specialty specialty);

    void delete(long id);

    List<Specialty> search(String searchterm);

    void resetSearchIndex();

    Specialty findSpecialtyByName(String name);

}
