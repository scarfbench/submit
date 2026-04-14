package org.woehlke.jakartaee.petclinic.owner.db;

import org.woehlke.jakartaee.petclinic.application.framework.db.CrudDao;
import org.woehlke.jakartaee.petclinic.owner.Owner;

import java.util.List;

public interface OwnerDao extends CrudDao<Owner> {
    List<Owner> search(String searchterm);
    void resetSearchIndex();
}
