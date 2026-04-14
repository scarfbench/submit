package org.woehlke.jakartaee.petclinic.pettype.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.pettype.PetType;

import java.util.List;
import java.util.UUID;

@Log
@Service
@Transactional
public class PetTypeServiceImpl implements PetTypeService {

    private final PetTypeRepository petTypeRepository;

    @Autowired
    public PetTypeServiceImpl(PetTypeRepository petTypeRepository) {
        this.petTypeRepository = petTypeRepository;
    }

    @Override
    public List<PetType> getAll() {
        return petTypeRepository.findAllByOrderByNameAsc();
    }

    @Override
    public PetType findById(long id) {
        return petTypeRepository.findById(id).orElse(null);
    }

    @Override
    public PetType addNew(PetType petType) {
        petType.setUuid(UUID.randomUUID());
        log.info("addNew PetType: " + petType.toString());
        return petTypeRepository.save(petType);
    }

    @Override
    public PetType update(PetType petType) {
        log.info("update PetType: " + petType.toString());
        return petTypeRepository.save(petType);
    }

    @Override
    public void delete(long id) {
        petTypeRepository.deleteById(id);
    }

    @Override
    public List<PetType> search(String searchterm) {
        return petTypeRepository.search(searchterm);
    }

    @Override
    public PetType findByName(String name) {
        return petTypeRepository.findByName(name).orElse(null);
    }
}
