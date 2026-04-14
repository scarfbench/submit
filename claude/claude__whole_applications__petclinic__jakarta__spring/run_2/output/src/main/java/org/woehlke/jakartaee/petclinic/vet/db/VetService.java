package org.woehlke.jakartaee.petclinic.vet.db;

import org.woehlke.jakartaee.petclinic.specialty.db.Specialty;

import java.util.List;

public interface VetService {

    List<Vet> getAll();

    Vet findById(long id);

    Vet addNew(Vet vet);

    Vet update(Vet vet);

    void delete(long id);

    List<Vet> search(String searchterm);

    Vet addSpecialty(Vet vet, Specialty specialty);

    Vet removeSpecialty(Vet vet, Specialty specialty);
}
