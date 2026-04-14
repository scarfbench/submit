package com.example.addressbook.service;

import com.example.addressbook.entity.Contact;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class ContactService {

    @PersistenceContext(unitName = "addressBookPU")
    private EntityManager em;

    public void create(Contact entity) {
        em.persist(entity);
    }

    public void edit(Contact entity) {
        em.merge(entity);
    }

    public void remove(Contact entity) {
        if (entity == null) return;
        // Re-attach if detached
        Contact managed = em.find(Contact.class, entity.getId());
        if (managed != null) {
            em.remove(managed);
        }
    }

    public Contact find(Long id) {
        return (id == null) ? null : em.find(Contact.class, id);
    }

    public List<Contact> findAll() {
        TypedQuery<Contact> q = em.createQuery("SELECT c FROM Contact c", Contact.class);
        return q.getResultList();
    }

    public List<Contact> findRange(int[] range) {
        int start = range[0];
        int endExclusive = range[1];
        int size = Math.max(0, endExclusive - start);
        if (size <= 0) return List.of();
        TypedQuery<Contact> q = em.createQuery("SELECT c FROM Contact c", Contact.class);
        q.setFirstResult(start);
        q.setMaxResults(size);
        return q.getResultList();
    }

    public int count() {
        Long c = em.createQuery("SELECT COUNT(c) FROM Contact c", Long.class).getSingleResult();
        return (c > Integer.MAX_VALUE) ? Integer.MAX_VALUE : c.intValue();
    }
}
