package org.woehlke.jakartaee.petclinic.owner.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.pet.db.PetRepository;
import org.woehlke.jakartaee.petclinic.visit.Visit;
import org.woehlke.jakartaee.petclinic.visit.db.VisitRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.UUID;

/**
 * Created by tw on 10.03.14.
 */
@Log
@Service
@Transactional
public class OwnerServiceImpl implements OwnerService {

    private static final long serialVersionUID = -553095693269912269L;

    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final VisitRepository visitRepository;

    @Autowired
    public OwnerServiceImpl(OwnerRepository ownerRepository, PetRepository petRepository, VisitRepository visitRepository) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.visitRepository = visitRepository;
    }

    @Override
    public Visit addNewVisit(Visit visit) {
        visit.setUuid(UUID.randomUUID());
        log.info("addNew Visit: " + visit.toString());
        return visitRepository.save(visit);
    }

    @Override
    public List<Pet> getPetsAsList(Owner owner){
        return petRepository.findByOwner(owner);
    }

    @Override
    public String getPetsAsString(Owner owner) {
        StringBuilder s = new StringBuilder();
        for (Pet pet : this.getPetsAsList(owner)) {
            s.append(pet.getName())
                    .append(" (")
                    .append(pet.getType().getName())
                    .append(") ");
        }
        return s.toString();
    }

    @Override
    public void resetSearchIndex() {
        // no-op
    }

    @Override
    public List<Owner> getAll() {
        return this.ownerRepository.findAllOrderByName();
    }

    @Override
    public void delete(long id) {
        log.info("delete Owner: " + id);
        this.ownerRepository.deleteById(id);
    }

    @Override
    public Owner addNew(Owner owner) {
        owner.setUuid(UUID.randomUUID());
        log.info("addNew Owner: " + owner.toString());
        return this.ownerRepository.save(owner);
    }

    @Override
    public Owner findById(long id) {
        return this.ownerRepository.findById(id).orElse(null);
    }

    @Override
    public Owner update(Owner owner) {
        log.info("update Owner: " + owner.toString());
        return this.ownerRepository.save(owner);
    }

    @Override
    public List<Owner> search(String searchterm) {
        return this.ownerRepository.search("%" + searchterm + "%");
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: "+OwnerServiceImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: "+OwnerServiceImpl.class.getSimpleName());
    }
}
