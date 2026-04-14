package org.woehlke.jakartaee.petclinic.pettype.db;

import java.util.List;

public interface PetTypeService {

    List<PetType> getAll();

    PetType findById(long id);

    PetType addNew(PetType petType);

    PetType update(PetType petType);

    void delete(long id);

    List<PetType> search(String searchterm);
}
