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

import java.util.List;
import java.util.UUID;

@Log
@Service
@Transactional
public class OwnerServiceImpl implements OwnerService {

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
    public List<Owner> getAll() {
        return ownerRepository.findAllByOrderByLastNameAscFirstNameAsc();
    }

    @Override
    public Owner findById(long id) {
        return ownerRepository.findById(id).orElse(null);
    }

    @Override
    public Owner addNew(Owner owner) {
        owner.setUuid(UUID.randomUUID());
        log.info("addNew Owner: " + owner.toString());
        return ownerRepository.save(owner);
    }

    @Override
    public Owner update(Owner owner) {
        log.info("update Owner: " + owner.toString());
        return ownerRepository.save(owner);
    }

    @Override
    public void delete(long id) {
        ownerRepository.deleteById(id);
    }

    @Override
    public List<Owner> search(String searchterm) {
        return ownerRepository.search(searchterm);
    }

    @Override
    public List<Pet> getPetsAsList(Owner owner) {
        return petRepository.findByOwnerOrderByNameAsc(owner);
    }

    @Override
    public Visit addNewVisit(Visit visit) {
        visit.setUuid(UUID.randomUUID());
        log.info("addNew Visit: " + visit.toString());
        return visitRepository.save(visit);
    }
}
