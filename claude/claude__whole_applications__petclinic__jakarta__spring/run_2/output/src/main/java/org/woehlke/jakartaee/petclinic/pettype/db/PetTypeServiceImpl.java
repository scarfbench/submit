package org.woehlke.jakartaee.petclinic.pettype.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (petType.getUuid() == null) {
            petType.setUuid(UUID.randomUUID());
        }
        return petTypeRepository.save(petType);
    }

    @Override
    public PetType update(PetType petType) {
        return petTypeRepository.save(petType);
    }

    @Override
    public void delete(long id) {
        petTypeRepository.deleteById(id);
    }

    @Override
    public List<PetType> search(String searchterm) {
        return petTypeRepository.findAll();
    }
}
