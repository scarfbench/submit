package org.woehlke.jakartaee.petclinic.vet.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.vet.Vet;

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
        vet.setUuid(UUID.randomUUID());
        log.info("addNew Vet: " + vet.toString());
        return vetRepository.save(vet);
    }

    @Override
    public Vet update(Vet vet) {
        log.info("update Vet: " + vet.toString());
        return vetRepository.save(vet);
    }

    @Override
    public void delete(long id) {
        vetRepository.deleteById(id);
    }

    @Override
    public List<Vet> search(String searchterm) {
        return vetRepository.search(searchterm);
    }
}
