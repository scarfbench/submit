package org.woehlke.jakartaee.petclinic.vet.db;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woehlke.jakartaee.petclinic.vet.Vet;

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
public class VetServiceImpl implements VetService {

    private static final long serialVersionUID = 2698313227542867286L;

    private final VetRepository vetRepository;

    @Autowired
    public VetServiceImpl(VetRepository vetRepository) {
        this.vetRepository = vetRepository;
    }

    @Override
    public List<Vet> getAll() {
        return vetRepository.findAllOrderByName();
    }

    @Override
    public Vet findById(long id) {
        return this.vetRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(long id) {
        log.info("delete Vet: " + id);
        this.vetRepository.deleteById(id);
    }

    @Override
    public Vet addNew(Vet vet) {
        vet.setUuid(UUID.randomUUID());
        log.info("addNew Vet: " + vet.toString());
        return this.vetRepository.save(vet);
    }

    @Override
    public Vet update(Vet vet) {
        log.info("update Vet: " + vet.toString());
        return this.vetRepository.save(vet);
    }

    @Override
    public List<Vet> search(String searchterm) {
        return this.vetRepository.search("%" + searchterm + "%");
    }

    @Override
    public void resetSearchIndex() {
        // no-op
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: "+VetServiceImpl.class.getCanonicalName() );
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: "+VetServiceImpl.class.getCanonicalName() );
    }
}
