package org.woehlke.jakartaee.petclinic.specialty.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.UUID;

@Log
@Service
@Transactional
public class SpecialtyServiceImpl implements SpecialtyService {

    private static final long serialVersionUID = 6145428275502469961L;

    private final SpecialtyRepository specialtyRepository;

    @Autowired
    public SpecialtyServiceImpl(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    @Override
    public List<Specialty> getAll() {
        return this.specialtyRepository.findAllOrderByName();
    }

    @Override
    public Specialty findById(long id) {
        return this.specialtyRepository.findById(id).orElse(null);
    }

    @Override
    public Specialty addNew(Specialty specialty) {
        specialty.setUuid(UUID.randomUUID());
        log.info("addNew Specialty: " + specialty.toString());
        return this.specialtyRepository.save(specialty);
    }

    @Override
    public Specialty update(Specialty specialty) {
        log.info("update Specialty: " + specialty.toString());
        return this.specialtyRepository.save(specialty);
    }

    @Override
    public void delete(long id) {
        log.info("delete Specialty: " + id);
        this.specialtyRepository.deleteById(id);
    }

    @Override
    public List<Specialty> search(String searchterm) {
        return this.specialtyRepository.search("%" + searchterm + "%");
    }

    @Override
    public void resetSearchIndex() {
        // no-op
    }

    @Override
    public Specialty findSpecialtyByName(String name) {
        return this.specialtyRepository.findByName(name);
    }


    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: "+SpecialtyServiceImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: "+SpecialtyServiceImpl.class.getSimpleName());
    }
}
