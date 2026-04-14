package com.example.addressbookspring.service;

import com.example.addressbookspring.entity.Contact;
import com.example.addressbookspring.repository.ContactRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@Transactional
public class ContactService {

    @Inject
    ContactRepository repo;

    public void create(Contact entity) {
        repo.persist(entity);
    }

    public void edit(Contact entity) {
        repo.getEntityManager().merge(entity);
    }

    public void remove(Contact entity) {
        if (entity == null) return;
        Long id = entity.id;
        if (id != null) {
            Contact managed = repo.findById(id);
            if (managed != null) {
                repo.delete(managed);
            }
        }
    }

    public Contact find(Long id) {
        return (id == null) ? null : repo.findById(id);
    }

    public List<Contact> findAll() {
        return repo.listAll();
    }

    public List<Contact> findRange(int[] range) {
        int start = range[0];
        int endExclusive = range[1];
        int size = Math.max(0, endExclusive - start);
        if (size <= 0) return List.of();
        int page = start / size;
        return repo.findAll().page(Page.of(page, size)).list();
    }

    public int count() {
        long c = repo.count();
        return (c > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) c;
    }
}
