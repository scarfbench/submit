package org.woehlke.jakartaee.petclinic.visit.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;

@Log
@ApplicationScoped
public class VisitServiceImpl implements VisitService {

    @Inject
    VisitDao visitDao;

    @Override
    public List<Visit> getAll() { return this.visitDao.getAll(); }

    @Override
    public Visit findById(long id) { return this.visitDao.findById(id); }

    @Override
    @Transactional
    public Visit addNew(Visit visit) {
        log.info("addNew Visit: " + visit.toString());
        return this.visitDao.addNew(visit);
    }

    @Override
    @Transactional
    public Visit update(Visit visit) {
        log.info("update Visit: " + visit.toString());
        return this.visitDao.update(visit);
    }

    @Override
    @Transactional
    public void delete(long id) { this.visitDao.delete(id); }

    @Override
    public List<Visit> getVisits(Pet pet) { return this.visitDao.getVisits(pet); }

    @PostConstruct
    public void postConstruct() { log.info("postConstruct: " + VisitServiceImpl.class.getSimpleName()); }

    @PreDestroy
    public void preDestroy() { log.info("preDestroy: " + VisitServiceImpl.class.getSimpleName()); }
}
