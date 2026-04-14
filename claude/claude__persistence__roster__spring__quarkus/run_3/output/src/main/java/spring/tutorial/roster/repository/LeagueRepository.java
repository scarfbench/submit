package spring.tutorial.roster.repository;

import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import jakartaee.tutorial.roster.entity.League;

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

    public void deleteById(String id) {
        League l = em.find(League.class, id);
        if (l != null) {
            em.remove(l);
        }
    }
}
