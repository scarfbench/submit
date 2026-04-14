package org.woehlke.jakartaee.petclinic.pettype.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pettype.PetType;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;

@Log
@ApplicationScoped
public class PetTypeServiceImpl implements PetTypeService {

    @Inject
    PetTypeDao petTypeDao;

    @Override
    public List<PetType> getAll() { return this.petTypeDao.getAll(); }

    @Override
    public PetType findById(long id) { return this.petTypeDao.findById(id); }

    @Override
    @Transactional
    public PetType addNew(PetType petType) {
        log.info("addNew PetType: " + petType.toString());
        return this.petTypeDao.addNew(petType);
    }

    @Override
    @Transactional
    public PetType update(PetType petType) {
        log.info("update PetType: " + petType.toString());
        return this.petTypeDao.update(petType);
    }

    @Override
    @Transactional
    public void delete(long id) { this.petTypeDao.delete(id); }

    @Override
    public PetType findByName(String name) { return this.petTypeDao.findByName(name); }

    @Override
    public List<PetType> search(String searchterm) { return this.petTypeDao.search(searchterm); }

    @Override
    public void resetSearchIndex() {}

    @PostConstruct
    public void postConstruct() { log.info("postConstruct: " + PetTypeServiceImpl.class.getSimpleName()); }

    @PreDestroy
    public void preDestroy() { log.info("preDestroy: " + PetTypeServiceImpl.class.getSimpleName()); }
}
