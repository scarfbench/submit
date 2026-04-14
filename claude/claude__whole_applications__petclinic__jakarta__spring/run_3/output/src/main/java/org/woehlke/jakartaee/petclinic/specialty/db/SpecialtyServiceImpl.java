package org.woehlke.jakartaee.petclinic.specialty.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import java.util.List;
import java.util.UUID;

@Log
@Service
@Transactional
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    @Autowired
    public SpecialtyServiceImpl(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    @Override
    public List<Specialty> getAll() {
        return specialtyRepository.findAllByOrderByNameAsc();
    }

    @Override
    public Specialty findById(long id) {
        return specialtyRepository.findById(id).orElse(null);
    }

    @Override
    public Specialty addNew(Specialty specialty) {
        specialty.setUuid(UUID.randomUUID());
        specialty.updateSearchindex();
        log.info("addNew Specialty: " + specialty.toString());
        return specialtyRepository.save(specialty);
    }

    @Override
    public Specialty update(Specialty specialty) {
        specialty.updateSearchindex();
        log.info("update Specialty: " + specialty.toString());
        return specialtyRepository.save(specialty);
    }

    @Override
    public void delete(long id) {
        specialtyRepository.deleteById(id);
    }

    @Override
    public List<Specialty> search(String searchterm) {
        return specialtyRepository.search(searchterm);
    }

    @Override
    public Specialty findSpecialtyByName(String name) {
        return specialtyRepository.findByName(name).orElse(null);
    }
}
