package spring.tutorial.roster.repository;

import java.util.List;
import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import jakartaee.tutorial.roster.entity.Team;

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

    public void saveAll(List<Team> teams) {
        for (Team t : teams) {
            save(t);
        }
    }

    public Optional<Team> findById(String id) {
        return Optional.ofNullable(em.find(Team.class, id));
    }

    public void deleteById(String id) {
        Team t = em.find(Team.class, id);
        if (t != null) {
            em.remove(t);
        }
    }

    public List<Team> findByLeague_Id(String leagueId) {
        return em.createQuery(
                "SELECT t FROM Team t WHERE t.league.id = :leagueId", Team.class)
                .setParameter("leagueId", leagueId)
                .getResultList();
    }
}
