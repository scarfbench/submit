package org.woehlke.jakartaee.petclinic.vet.db;

import org.woehlke.jakartaee.petclinic.application.framework.db.CrudService;
import org.woehlke.jakartaee.petclinic.vet.Vet;

import java.util.List;

public interface VetService extends CrudService<Vet> {
    List<Vet> search(String searchterm);
    void resetSearchIndex();
}
