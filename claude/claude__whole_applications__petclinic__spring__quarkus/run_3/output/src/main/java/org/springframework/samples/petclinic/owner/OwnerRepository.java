package org.springframework.samples.petclinic.owner;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

/**
 * Repository class for Owner domain objects.
 */
@ApplicationScoped
public class OwnerRepository {

	@Inject
	EntityManager em;

	public Optional<Owner> findById(Integer id) {
		Owner owner = em.find(Owner.class, id);
		return Optional.ofNullable(owner);
	}

	public List<Owner> findByLastNameStartingWith(String lastName, int page, int pageSize) {
		TypedQuery<Owner> query = em.createQuery(
				"SELECT o FROM Owner o WHERE LOWER(o.lastName) LIKE LOWER(:lastName) ORDER BY o.lastName",
				Owner.class);
		query.setParameter("lastName", lastName + "%");
		query.setFirstResult((page - 1) * pageSize);
		query.setMaxResults(pageSize);
		return query.getResultList();
	}

	public long countByLastNameStartingWith(String lastName) {
		TypedQuery<Long> query = em.createQuery(
				"SELECT COUNT(o) FROM Owner o WHERE LOWER(o.lastName) LIKE LOWER(:lastName)",
				Long.class);
		query.setParameter("lastName", lastName + "%");
		return query.getSingleResult();
	}

	@Transactional
	public Owner save(Owner owner) {
		if (owner.isNew()) {
			em.persist(owner);
			return owner;
		} else {
			return em.merge(owner);
		}
	}

}
