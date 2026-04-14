package org.woehlke.jakartaee.petclinic.vet.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.specialty.db.Specialty;

import java.util.List;
import java.util.UUID;

@Log
@Service
@Transactional
public class VetServiceImpl implements VetService {

    private final VetRepository vetRepository;

    @Autowired
    public VetServiceImpl(VetRepository vetRepository) {
        this.vetRepository = vetRepository;
    }

    @Override
    public List<Vet> getAll() {
        return vetRepository.findAllByOrderByLastNameAscFirstNameAsc();
    }

    @Override
    public Vet findById(long id) {
        return vetRepository.findById(id).orElse(null);
    }

    @Override
    public Vet addNew(Vet vet) {
        if (vet.getUuid() == null) {
            vet.setUuid(UUID.randomUUID());
        }
        return vetRepository.save(vet);
    }

    @Override
    public Vet update(Vet vet) {
        return vetRepository.save(vet);
    }

    @Override
    public void delete(long id) {
        vetRepository.deleteById(id);
    }

    @Override
    public List<Vet> search(String searchterm) {
        String term = "%" + searchterm.toLowerCase() + "%";
        return vetRepository.search(term);
    }

    @Override
    public Vet addSpecialty(Vet vet, Specialty specialty) {
        vet.getSpecialties().add(specialty);
        return vetRepository.save(vet);
    }

    @Override
    public Vet removeSpecialty(Vet vet, Specialty specialty) {
        vet.getSpecialties().remove(specialty);
        return vetRepository.save(vet);
    }
}
