package spring.tutorial.roster.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakartaee.tutorial.roster.entity.Team;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TeamRepository {

    @Inject
    EntityManager em;

    public Team save(Team team) {
        if (em.find(Team.class, team.getId()) != null) {
            return em.merge(team);
        }
        em.persist(team);
        return team;
    }

    public Optional<Team> findById(String id) {
        return Optional.ofNullable(em.find(Team.class, id));
    }

    public List<Team> findAll() {
        return em.createQuery("SELECT t FROM Team t", Team.class).getResultList();
    }

    public void deleteById(String id) {
        Team team = em.find(Team.class, id);
        if (team != null) {
            em.remove(team);
        }
    }

    public List<Team> findByLeague_Id(String leagueId) {
        TypedQuery<Team> q = em.createQuery(
            "SELECT t FROM Team t WHERE t.league.id = :leagueId", Team.class);
        q.setParameter("leagueId", leagueId);
        return q.getResultList();
    }

    public void saveAll(Iterable<? extends Team> teams) {
        for (Team t : teams) {
            save(t);
        }
    }
}
