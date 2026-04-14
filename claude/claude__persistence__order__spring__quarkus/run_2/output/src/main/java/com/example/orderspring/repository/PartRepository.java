package com.example.orderspring.repository;

import com.example.orderspring.entity.Part;
import com.example.orderspring.entity.PartKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PartRepository {

    @Inject
    EntityManager em;

    public List<Part> findAllParts() {
        return em.createQuery("SELECT p FROM Part p ORDER BY p.partNumber", Part.class)
                .getResultList();
    }

    public Optional<Part> findByPartNumberAndRevision(String partNumber, int revision) {
        PartKey key = new PartKey(partNumber, revision);
        Part part = em.find(Part.class, key);
        return Optional.ofNullable(part);
    }

    public Part save(Part part) {
        PartKey key = new PartKey(part.getPartNumber(), part.getRevision());
        Part existing = em.find(Part.class, key);
        if (existing == null) {
            em.persist(part);
            return part;
        } else {
            return em.merge(part);
        }
    }
}
