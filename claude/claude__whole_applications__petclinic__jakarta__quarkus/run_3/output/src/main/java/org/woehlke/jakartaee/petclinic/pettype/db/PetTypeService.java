package org.woehlke.jakartaee.petclinic.pettype.db;

import org.woehlke.jakartaee.petclinic.application.framework.db.CrudService;
import org.woehlke.jakartaee.petclinic.pettype.PetType;

import java.util.List;

public interface PetTypeService extends CrudService<PetType> {
    PetType findByName(String name);
    List<PetType> search(String searchterm);
    void resetSearchIndex();
}
