package org.woehlke.jakartaee.petclinic.specialty.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;

@Log
@ApplicationScoped
public class SpecialtyServiceImpl implements SpecialtyService {

    @Inject
    SpecialtyDao specialtyDao;

    @Override
    public List<Specialty> getAll() { return this.specialtyDao.getAll(); }

    @Override
    public Specialty findById(long id) { return this.specialtyDao.findById(id); }

    @Override
    @Transactional
    public Specialty addNew(Specialty specialty) {
        log.info("addNew Specialty: " + specialty.toString());
        return this.specialtyDao.addNew(specialty);
    }

    @Override
    @Transactional
    public Specialty update(Specialty specialty) {
        log.info("update Specialty: " + specialty.toString());
        return this.specialtyDao.update(specialty);
    }

    @Override
    @Transactional
    public void delete(long id) { this.specialtyDao.delete(id); }

    @Override
    public Specialty findSpecialtyByName(String name) { return this.specialtyDao.findSpecialtyByName(name); }

    @Override
    public List<Specialty> search(String searchterm) { return this.specialtyDao.search(searchterm); }

    @Override
    public void resetSearchIndex() {}

    @PostConstruct
    public void postConstruct() { log.info("postConstruct: " + SpecialtyServiceImpl.class.getSimpleName()); }

    @PreDestroy
    public void preDestroy() { log.info("preDestroy: " + SpecialtyServiceImpl.class.getSimpleName()); }
}
