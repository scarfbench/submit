package com.example.addressbookspring.service;

import com.example.addressbookspring.entity.Contact;
import com.example.addressbookspring.repo.ContactRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Service layer for Contact entity.
 * Migrated from Spring @Service/@Transactional to Jakarta CDI/JTA.
 */
@ApplicationScoped
@Transactional
public class ContactService {

    @Inject
    private ContactRepository repo;

    public ContactService() {
        // Default no-arg constructor required by CDI
    }

    public void create(Contact entity) {
        repo.save(entity);
    }

    public void edit(Contact entity) {
        repo.save(entity);
    }

    public void remove(Contact entity) {
        if (entity == null) return;
        Long id = entity.getId();
        if (id != null && repo.existsById(id)) {
            repo.deleteById(id);
        } else {
            repo.delete(entity);
        }
    }

    public Contact find(Long id) {
        return (id == null) ? null : repo.findById(id);
    }

    public List<Contact> findAll() {
        return repo.findAll();
    }

    public List<Contact> findRange(int[] range) {
        int start = range[0];
        int endExclusive = range[1];
        int size = Math.max(0, endExclusive - start);
        if (size <= 0) return List.of();
        return repo.findAll(start, size);
    }

    public int count() {
        long c = repo.count();
        return (c > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) c;
    }
}
