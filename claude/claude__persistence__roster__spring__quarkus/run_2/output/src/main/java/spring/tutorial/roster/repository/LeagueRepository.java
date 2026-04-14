package spring.tutorial.roster.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakartaee.tutorial.roster.entity.League;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LeagueRepository {

    @Inject
    EntityManager em;

    public League save(League league) {
        if (em.find(League.class, league.getId()) != null) {
            return em.merge(league);
        }
        em.persist(league);
        return league;
    }

    public Optional<League> findById(String id) {
        return Optional.ofNullable(em.find(League.class, id));
    }

    public List<League> findAll() {
        return em.createQuery("SELECT l FROM League l", League.class).getResultList();
    }

    public void deleteById(String id) {
        League league = em.find(League.class, id);
        if (league != null) {
            em.remove(league);
        }
    }
}
