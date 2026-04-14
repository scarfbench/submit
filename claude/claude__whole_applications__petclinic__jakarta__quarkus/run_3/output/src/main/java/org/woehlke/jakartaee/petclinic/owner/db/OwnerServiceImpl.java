package org.woehlke.jakartaee.petclinic.owner.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pet.db.PetDao;
import org.woehlke.jakartaee.petclinic.visit.db.VisitDao;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;

@Log
@ApplicationScoped
public class OwnerServiceImpl implements OwnerService {

    @Inject
    OwnerDao ownerDao;

    @Inject
    PetDao petDao;

    @Inject
    VisitDao visitDao;

    @Override
    @Transactional
    public Visit addNewVisit(Visit visit) {
        log.info("addNew Visit: " + visit.toString());
        Pet pet = visit.getPet();
        Owner owner = pet.getOwner();
        visit.setPet(null);
        visit = visitDao.addNew(visit);
        owner = ownerDao.update(owner);
        pet.setOwner(owner);
        pet = petDao.update(pet);
        visit.setPet(pet);
        visit = visitDao.update(visit);
        return visit;
    }

    @Override
    public List<Pet> getPetsAsList(Owner owner) { return petDao.getPetsAsList(owner); }

    @Override
    public String getPetsAsString(Owner owner) {
        StringBuilder s = new StringBuilder();
        for (Pet pet : this.getPetsAsList(owner)) {
            s.append(pet.getName()).append(" (").append(pet.getType().getName()).append(") ");
        }
        return s.toString();
    }

    @Override
    public void resetSearchIndex() {
        for (Owner owner : this.getAll()) {
            for (Pet pet : this.getPetsAsList(owner)) {
                for (Visit visit : visitDao.getVisits(pet)) { this.visitDao.update(visit); }
                this.petDao.update(pet);
            }
            this.ownerDao.update(owner);
        }
    }

    @Override
    public List<Owner> getAll() { return this.ownerDao.getAll(); }

    @Override
    @Transactional
    public void delete(long id) { this.ownerDao.delete(id); }

    @Override
    @Transactional
    public Owner addNew(Owner owner) {
        log.info("addNew Owner: " + owner.toString());
        return this.ownerDao.addNew(owner);
    }

    @Override
    public Owner findById(long id) { return this.ownerDao.findById(id); }

    @Override
    @Transactional
    public Owner update(Owner owner) {
        log.info("update Owner: " + owner.toString());
        return this.ownerDao.update(owner);
    }

    @Override
    public List<Owner> search(String searchterm) { return this.ownerDao.search(searchterm); }

    @PostConstruct
    public void postConstruct() { log.info("postConstruct: " + OwnerServiceImpl.class.getSimpleName()); }

    @PreDestroy
    public void preDestroy() { log.info("preDestroy: " + OwnerServiceImpl.class.getSimpleName()); }
}
