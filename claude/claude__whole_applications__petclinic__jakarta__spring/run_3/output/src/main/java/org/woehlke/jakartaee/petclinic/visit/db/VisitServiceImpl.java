package org.woehlke.jakartaee.petclinic.visit.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

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
        return visitRepository.findAllByOrderByDateAsc();
    }

    @Override
    public Visit findById(long id) {
        return visitRepository.findById(id).orElse(null);
    }

    @Override
    public Visit addNew(Visit visit) {
        visit.setUuid(UUID.randomUUID());
        log.info("addNew Visit: " + visit.toString());
        return visitRepository.save(visit);
    }

    @Override
    public Visit update(Visit visit) {
        log.info("update Visit: " + visit.toString());
        return visitRepository.save(visit);
    }

    @Override
    public void delete(long id) {
        visitRepository.deleteById(id);
    }

    @Override
    public List<Visit> getAllVisitsOfAnPet(Pet pet) {
        return visitRepository.findByPetOrderByDateAsc(pet);
    }
}
