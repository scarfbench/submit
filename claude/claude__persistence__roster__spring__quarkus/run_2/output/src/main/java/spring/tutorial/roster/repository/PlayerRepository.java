package spring.tutorial.roster.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakartaee.tutorial.roster.entity.Player;

import java.util.List;
import java.util.Optional;

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

    public List<Player> findAll() {
        return em.createQuery("SELECT p FROM Player p", Player.class).getResultList();
    }

    public void deleteById(String id) {
        Player player = em.find(Player.class, id);
        if (player != null) {
            em.remove(player);
        }
    }

    public List<Player> findByPosition(String position) {
        TypedQuery<Player> q = em.createQuery(
            "SELECT p FROM Player p WHERE p.position = :position", Player.class);
        q.setParameter("position", position);
        return q.getResultList();
    }

    public List<Player> findBySalaryBetween(double low, double high) {
        TypedQuery<Player> q = em.createQuery(
            "SELECT p FROM Player p WHERE p.salary BETWEEN :low AND :high", Player.class);
        q.setParameter("low", low);
        q.setParameter("high", high);
        return q.getResultList();
    }

    public List<Player> findBySalaryGreaterThan(double salary) {
        TypedQuery<Player> q = em.createQuery(
            "SELECT p FROM Player p WHERE p.salary > :salary", Player.class);
        q.setParameter("salary", salary);
        return q.getResultList();
    }

    public List<Player> findByTeams_Id(String teamId) {
        TypedQuery<Player> q = em.createQuery(
            "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.id = :teamId", Player.class);
        q.setParameter("teamId", teamId);
        return q.getResultList();
    }

    public List<Player> findByTeamsIsEmpty() {
        TypedQuery<Player> q = em.createQuery(
            "SELECT p FROM Player p WHERE p.teams IS EMPTY", Player.class);
        return q.getResultList();
    }

    public List<Player> findByPositionAndName(String position, String name) {
        TypedQuery<Player> q = em.createQuery(
            "SELECT p FROM Player p WHERE p.position = :position AND p.name = :name", Player.class);
        q.setParameter("position", position);
        q.setParameter("name", name);
        return q.getResultList();
    }

    public List<Player> findDistinctByTeams_League_Id(String leagueId) {
        TypedQuery<Player> q = em.createQuery(
            "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.id = :leagueId", Player.class);
        q.setParameter("leagueId", leagueId);
        return q.getResultList();
    }

    public List<Player> findDistinctByTeams_League_Sport(String sport) {
        TypedQuery<Player> q = em.createQuery(
            "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.sport = :sport", Player.class);
        q.setParameter("sport", sport);
        return q.getResultList();
    }

    public List<Player> findDistinctByTeams_City(String city) {
        TypedQuery<Player> q = em.createQuery(
            "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.city = :city", Player.class);
        q.setParameter("city", city);
        return q.getResultList();
    }

    public Optional<Player> findFirstByName(String name) {
        TypedQuery<Player> q = em.createQuery(
            "SELECT p FROM Player p WHERE p.name = :name", Player.class);
        q.setParameter("name", name);
        q.setMaxResults(1);
        List<Player> result = q.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public void saveAll(Iterable<? extends Player> players) {
        for (Player p : players) {
            save(p);
        }
    }
}
