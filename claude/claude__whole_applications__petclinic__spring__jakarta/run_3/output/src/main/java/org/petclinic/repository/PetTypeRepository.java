package org.petclinic.repository;

import java.util.List;

import org.petclinic.model.PetType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@ApplicationScoped
public class PetTypeRepository {

    @PersistenceContext(unitName = "petclinicPU")
    private EntityManager em;

    public List<PetType> findPetTypes() {
        TypedQuery<PetType> query = em.createQuery(
                "SELECT ptype FROM PetType ptype ORDER BY ptype.name", PetType.class);
        return query.getResultList();
    }

    public PetType findById(int id) {
        return em.find(PetType.class, id);
    }
}
