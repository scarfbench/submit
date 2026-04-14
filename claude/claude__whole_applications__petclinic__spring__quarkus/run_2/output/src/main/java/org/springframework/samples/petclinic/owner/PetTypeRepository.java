package org.springframework.samples.petclinic.owner;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@ApplicationScoped
public class PetTypeRepository {

	@Inject
	EntityManager em;

	public List<PetType> findPetTypes() {
		TypedQuery<PetType> query = em.createQuery(
			"SELECT ptype FROM PetType ptype ORDER BY ptype.name", PetType.class);
		return query.getResultList();
	}

	public PetType findById(Integer id) {
		return em.find(PetType.class, id);
	}

}
