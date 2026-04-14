package org.woehlke.jakartaee.petclinic.pettype.db;

import org.woehlke.jakartaee.petclinic.pettype.PetType;

import java.util.List;

/**
 *
 */
public interface PetTypeService {

    long serialVersionUID = 6637453269836393L;

    List<PetType> getAll();

    void delete(long id);

    PetType addNew(PetType petType);

    PetType findById(long id);

    PetType update(PetType petType);

    List<PetType> search(String searchterm);

    void resetSearchIndex();

    PetType findByName(String name);
}
