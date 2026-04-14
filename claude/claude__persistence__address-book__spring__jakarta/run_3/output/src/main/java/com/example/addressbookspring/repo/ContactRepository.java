package com.example.addressbookspring.repo;

import com.example.addressbookspring.entity.Contact;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Repository for Contact entity using Jakarta Persistence (JPA).
 * Replaces Spring Data JPA's JpaRepository with direct EntityManager usage.
 */
@ApplicationScoped
public class ContactRepository {

    @PersistenceContext(unitName = "addressBookPU")
    private EntityManager em;

    public Contact save(Contact entity) {
        if (entity.getId() == null) {
            em.persist(entity);
            return entity;
        } else {
            return em.merge(entity);
        }
    }

    public Contact findById(Long id) {
        return em.find(Contact.class, id);
    }

    public boolean existsById(Long id) {
        return em.find(Contact.class, id) != null;
    }

    public void deleteById(Long id) {
        Contact entity = em.find(Contact.class, id);
        if (entity != null) {
            em.remove(entity);
        }
    }

    public void delete(Contact entity) {
        if (!em.contains(entity)) {
            entity = em.merge(entity);
        }
        em.remove(entity);
    }

    public List<Contact> findAll() {
        TypedQuery<Contact> query = em.createQuery("SELECT c FROM Contact c", Contact.class);
        return query.getResultList();
    }

    public List<Contact> findAll(int offset, int maxResults) {
        TypedQuery<Contact> query = em.createQuery("SELECT c FROM Contact c ORDER BY c.id", Contact.class);
        query.setFirstResult(offset);
        query.setMaxResults(maxResults);
        return query.getResultList();
    }

    public long count() {
        return em.createQuery("SELECT COUNT(c) FROM Contact c", Long.class).getSingleResult();
    }
}
