package org.woehlke.jakartaee.petclinic.vet.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.vet.Vet;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;

@Log
@ApplicationScoped
public class VetServiceImpl implements VetService {

    @Inject
    VetDao vetDao;

    @Override
    public List<Vet> getAll() { return this.vetDao.getAll(); }

    @Override
    public Vet findById(long id) { return this.vetDao.findById(id); }

    @Override
    @Transactional
    public Vet addNew(Vet vet) {
        log.info("addNew Vet: " + vet.toString());
        return this.vetDao.addNew(vet);
    }

    @Override
    @Transactional
    public Vet update(Vet vet) {
        log.info("update Vet: " + vet.toString());
        return this.vetDao.update(vet);
    }

    @Override
    @Transactional
    public void delete(long id) { this.vetDao.delete(id); }

    @Override
    public List<Vet> search(String searchterm) { return this.vetDao.search(searchterm); }

    @Override
    public void resetSearchIndex() {}

    @PostConstruct
    public void postConstruct() { log.info("postConstruct: " + VetServiceImpl.class.getSimpleName()); }

    @PreDestroy
    public void preDestroy() { log.info("preDestroy: " + VetServiceImpl.class.getSimpleName()); }
}
