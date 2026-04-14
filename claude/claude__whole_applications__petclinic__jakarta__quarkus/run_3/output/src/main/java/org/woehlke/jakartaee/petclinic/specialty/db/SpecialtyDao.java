package org.woehlke.jakartaee.petclinic.specialty.db;

import org.woehlke.jakartaee.petclinic.application.framework.db.CrudDao;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import java.util.List;

public interface SpecialtyDao extends CrudDao<Specialty> {
    Specialty findSpecialtyByName(String name);
    List<Specialty> search(String searchterm);
    void resetSearchIndex();
}
