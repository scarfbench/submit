package spring.tutorial.roster.repository;

import java.util.List;
import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import jakartaee.tutorial.roster.entity.Player;

@ApplicationScoped
public class PlayerRepository {

    @Inject
    EntityManager em;

    public Player save(Player player) {
        if (em.find(Player.class, player.getId()) != null) {
            return em.merge(player);
        }
        em.persist(player);
        return player;
    }

    public Optional<Player> findById(String id) {
        return Optional.ofNullable(em.find(Player.class, id));
    }

    public void deleteById(String id) {
        Player p = em.find(Player.class, id);
        if (p != null) {
            em.remove(p);
        }
    }

    public List<Player> findAll() {
        return em.createQuery("SELECT p FROM Player p", Player.class).getResultList();
    }

    public List<Player> findByPosition(String position) {
        return em.createQuery("SELECT p FROM Player p WHERE p.position = :pos", Player.class)
                .setParameter("pos", position)
                .getResultList();
    }

    public List<Player> findBySalaryBetween(double low, double high) {
        return em.createQuery("SELECT p FROM Player p WHERE p.salary BETWEEN :low AND :high", Player.class)
                .setParameter("low", low)
                .setParameter("high", high)
                .getResultList();
    }

    public List<Player> findBySalaryGreaterThan(double salary) {
        return em.createQuery("SELECT p FROM Player p WHERE p.salary > :salary", Player.class)
                .setParameter("salary", salary)
                .getResultList();
    }

    public List<Player> findByTeams_Id(String teamId) {
        return em.createQuery(
                "SELECT p FROM Player p JOIN p.teams t WHERE t.id = :teamId", Player.class)
                .setParameter("teamId", teamId)
                .getResultList();
    }

    public List<Player> findByTeamsIsEmpty() {
        return em.createQuery("SELECT p FROM Player p WHERE p.teams IS EMPTY", Player.class)
                .getResultList();
    }

    public List<Player> findByPositionAndName(String position, String name) {
        return em.createQuery(
                "SELECT p FROM Player p WHERE p.position = :pos AND p.name = :name", Player.class)
                .setParameter("pos", position)
                .setParameter("name", name)
                .getResultList();
    }

    public List<Player> findDistinctByTeams_League_Id(String leagueId) {
        return em.createQuery(
                "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.id = :leagueId", Player.class)
                .setParameter("leagueId", leagueId)
                .getResultList();
    }

    public List<Player> findDistinctByTeams_League_Sport(String sport) {
        return em.createQuery(
                "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.sport = :sport", Player.class)
                .setParameter("sport", sport)
                .getResultList();
    }

    public List<Player> findDistinctByTeams_City(String city) {
        return em.createQuery(
                "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.city = :city", Player.class)
                .setParameter("city", city)
                .getResultList();
    }

    public Optional<Player> findFirstByName(String name) {
        List<Player> results = em.createQuery(
                "SELECT p FROM Player p WHERE p.name = :name", Player.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
