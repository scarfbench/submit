package org.woehlke.jakartaee.petclinic.visit.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.pet.db.Pet;

import java.util.List;
import java.util.UUID;

@Log
@Service
@Transactional
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;

    @Autowired
    public VisitServiceImpl(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    public List<Visit> getAll() {
        return visitRepository.findAll();
    }

    @Override
    public Visit findById(long id) {
        return visitRepository.findById(id).orElse(null);
    }

    @Override
    public Visit addNew(Visit visit) {
        if (visit.getUuid() == null) {
            visit.setUuid(UUID.randomUUID());
        }
        return visitRepository.save(visit);
    }

    @Override
    public Visit update(Visit visit) {
        return visitRepository.save(visit);
    }

    @Override
    public void delete(long id) {
        visitRepository.deleteById(id);
    }

    @Override
    public List<Visit> getVisits(Pet pet) {
        return visitRepository.findByPetOrderByDateAsc(pet);
    }
}
