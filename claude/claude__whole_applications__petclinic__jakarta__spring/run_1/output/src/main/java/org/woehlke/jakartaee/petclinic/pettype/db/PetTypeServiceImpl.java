package org.woehlke.jakartaee.petclinic.pettype.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.pettype.PetType;

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
public class PetTypeServiceImpl implements PetTypeService  {

    private static final long serialVersionUID = -6242995649030237034L;

    private final PetTypeRepository petTypeRepository;

    @Autowired
    public PetTypeServiceImpl(PetTypeRepository petTypeRepository) {
        this.petTypeRepository = petTypeRepository;
    }

    @Override
    public List<PetType> getAll() {
        return petTypeRepository.findAllOrderByName();
    }

    @Override
    public void delete(long id) {
        log.info("delete PetType: " + id);
        this.petTypeRepository.deleteById(id);
    }

    @Override
    public PetType addNew(PetType petType) {
        petType.setUuid(UUID.randomUUID());
        log.info("addNew PetType: " + petType.toString());
        return this.petTypeRepository.save(petType);
    }

    @Override
    public PetType findById(long id) {
        return this.petTypeRepository.findById(id).orElse(null);
    }

    @Override
    public PetType update(PetType petType) {
        log.info("update PetType: " + petType.toString());
        return this.petTypeRepository.save(petType);
    }

    @Override
    public List<PetType> search(String searchterm) {
        return this.petTypeRepository.search("%" + searchterm + "%");
    }

    @Override
    public void resetSearchIndex() {
        // no-op
    }

    @Override
    public PetType findByName(String name) {
        return this.petTypeRepository.findByName(name);
    }


    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: "+PetTypeServiceImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: "+PetTypeServiceImpl.class.getSimpleName());
    }
}
