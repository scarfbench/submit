package org.woehlke.jakartaee.petclinic.vet.db;

import org.woehlke.jakartaee.petclinic.application.framework.db.CrudDao;
import org.woehlke.jakartaee.petclinic.vet.Vet;

import java.util.List;

public interface VetDao extends CrudDao<Vet> {
    List<Vet> search(String searchterm);
    void resetSearchIndex();
}
