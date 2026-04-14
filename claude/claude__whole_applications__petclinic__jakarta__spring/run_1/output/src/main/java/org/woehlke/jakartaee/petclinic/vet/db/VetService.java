package org.woehlke.jakartaee.petclinic.vet.db;

import org.woehlke.jakartaee.petclinic.vet.Vet;

import java.util.List;

/**
 *
 */
public interface VetService {

    long serialVersionUID = 6211608636423556157L;

    List<Vet> getAll();

    Vet findById(long id);

    void delete(long id);

    Vet addNew(Vet vet);

    Vet update(Vet vet);

    List<Vet> search(String searchterm);

    void resetSearchIndex();
}
