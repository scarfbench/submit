package org.woehlke.jakartaee.petclinic.visit.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.UUID;


/**
 *
 */
@Log
@Service
@Transactional
public class VisitServiceImpl implements VisitService {

    private static final long serialVersionUID = 4560958540651968289L;

    private final VisitRepository visitRepository;

    @Autowired
    public VisitServiceImpl(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    public List<Visit> getAll() {
        return this.visitRepository.findAllOrderByDate();
    }

    @Override
    public Visit findById(long id) {
        return this.visitRepository.findById(id).orElse(null);
    }

    @Override
    public Visit addNew(Visit visit) {
        visit.setUuid(UUID.randomUUID());
        log.info("addNew Visit: " + visit.toString());
        return this.visitRepository.save(visit);
    }

    @Override
    public Visit update(Visit visit) {
        log.info("update Visit: " + visit.toString());
        return this.visitRepository.save(visit);
    }

    @Override
    public void delete(long id) {
        log.info("delete: " + id);
        this.visitRepository.deleteById(id);
    }

    @Override
    public List<Visit> getAllVisitsOfAnPet(Pet pet) {
        return  this.visitRepository.findByPet(pet);
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: "+VisitServiceImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: "+VisitServiceImpl.class.getSimpleName());
    }


}
