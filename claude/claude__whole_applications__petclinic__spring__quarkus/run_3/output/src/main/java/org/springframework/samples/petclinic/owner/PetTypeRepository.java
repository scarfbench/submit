package org.springframework.samples.petclinic.owner;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

/**
 * Repository class for PetType domain objects.
 */
@ApplicationScoped
public class PetTypeRepository {

	@Inject
	EntityManager em;

	public List<PetType> findPetTypes() {
		TypedQuery<PetType> query = em.createQuery(
				"SELECT ptype FROM PetType ptype ORDER BY ptype.name", PetType.class);
		return query.getResultList();
	}

	public PetType findByName(String name) {
		TypedQuery<PetType> query = em.createQuery(
				"SELECT ptype FROM PetType ptype WHERE ptype.name = :name", PetType.class);
		query.setParameter("name", name);
		List<PetType> results = query.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

}
