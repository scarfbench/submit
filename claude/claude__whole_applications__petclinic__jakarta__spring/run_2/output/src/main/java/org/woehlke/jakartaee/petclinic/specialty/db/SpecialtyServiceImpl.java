package org.woehlke.jakartaee.petclinic.specialty.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (specialty.getUuid() == null) {
            specialty.setUuid(UUID.randomUUID());
        }
        return specialtyRepository.save(specialty);
    }

    @Override
    public Specialty update(Specialty specialty) {
        return specialtyRepository.save(specialty);
    }

    @Override
    public void delete(long id) {
        specialtyRepository.deleteById(id);
    }

    @Override
    public List<Specialty> search(String searchterm) {
        String term = "%" + searchterm.toLowerCase() + "%";
        return specialtyRepository.search(term);
    }
}
