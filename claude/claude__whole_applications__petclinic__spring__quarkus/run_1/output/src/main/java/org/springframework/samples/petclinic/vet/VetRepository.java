package org.springframework.samples.petclinic.vet;

import java.util.List;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class VetRepository {

	@Inject
	EntityManager em;

	@CacheResult(cacheName = "vets")
	public List<Vet> findAll() {
		return em.createQuery("SELECT v FROM Vet v", Vet.class).getResultList();
	}

	public List<Vet> findPaginated(int page, int pageSize) {
		return em.createQuery("SELECT v FROM Vet v", Vet.class)
			.setFirstResult((page - 1) * pageSize)
			.setMaxResults(pageSize)
			.getResultList();
	}

	public long count() {
		return em.createQuery("SELECT COUNT(v) FROM Vet v", Long.class).getSingleResult();
	}

}
