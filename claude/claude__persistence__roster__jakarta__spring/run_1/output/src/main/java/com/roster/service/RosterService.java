package com.roster.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roster.entity.League;
import com.roster.entity.Player;
import com.roster.entity.SummerLeague;
import com.roster.entity.Team;
import com.roster.entity.WinterLeague;
import com.roster.util.IncorrectSportException;
import com.roster.util.LeagueDetails;
import com.roster.util.PlayerDetails;
import com.roster.util.TeamDetails;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Service
@Transactional
public class RosterService {

    private static final Logger logger = Logger.getLogger("roster.service.RosterService");

    @PersistenceContext
    private EntityManager em;

    public void createPlayer(String id, String name, String position, double salary) {
        logger.info("createPlayer");
        Player player = new Player(id, name, position, salary);
        em.persist(player);
    }

    public void addPlayer(String playerId, String teamId) {
        logger.info("addPlayer");
        Player player = em.find(Player.class, playerId);
        Team team = em.find(Team.class, teamId);
        team.addPlayer(player);
        player.addTeam(team);
    }

    public void removePlayer(String playerId) {
        logger.info("removePlayer");
        Player player = em.find(Player.class, playerId);
        if (player == null) {
            throw new RuntimeException("Player not found: " + playerId);
        }
        Collection<Team> teams = player.getTeams();
        Iterator<Team> i = teams.iterator();
        while (i.hasNext()) {
            Team team = i.next();
            team.dropPlayer(player);
        }
        em.remove(player);
    }

    public void dropPlayer(String playerId, String teamId) {
        logger.info("dropPlayer");
        Player player = em.find(Player.class, playerId);
        Team team = em.find(Team.class, teamId);
        team.dropPlayer(player);
        player.dropTeam(team);
    }

    public PlayerDetails getPlayer(String playerId) {
        logger.info("getPlayerDetails");
        Player player = em.find(Player.class, playerId);
        if (player == null) {
            return null;
        }
        return new PlayerDetails(player.getId(),
                player.getName(),
                player.getPosition(),
                player.getSalary());
    }

    public List<PlayerDetails> getPlayersOfTeam(String teamId) {
        logger.info("getPlayersOfTeam");
        Team team = em.find(Team.class, teamId);
        return copyPlayersToDetails(new ArrayList<>(team.getPlayers()));
    }

    @Transactional(readOnly = true)
    public List<LeagueDetails> getAllLeagues() {
        logger.info("getAllLeagues");
        TypedQuery<League> q = em.createQuery(
                "SELECT l FROM League l ORDER BY l.id", League.class);
        List<League> leagues = q.getResultList();
        List<LeagueDetails> details = new ArrayList<>();
        for (League l : leagues) {
            details.add(new LeagueDetails(l.getId(), l.getName(), l.getSport()));
        }
        return details;
    }

    @Transactional(readOnly = true)
    public List<TeamDetails> getTeamsOfLeague(String leagueId) {
        logger.info("getTeamsOfLeague");
        League league = em.find(League.class, leagueId);
        List<TeamDetails> detailsList = new ArrayList<>();
        if (league != null && league.getTeams() != null) {
            for (Team team : league.getTeams()) {
                detailsList.add(new TeamDetails(team.getId(), team.getName(), team.getCity()));
            }
        }
        return detailsList;
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByPosition(String position) {
        logger.info("getPlayersByPosition");
        TypedQuery<Player> q = em.createQuery(
                "SELECT p FROM Player p WHERE p.position = :position", Player.class);
        q.setParameter("position", position);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByHigherSalary(String name) {
        logger.info("getPlayersByHigherSalary");
        TypedQuery<Player> q = em.createQuery(
                "SELECT DISTINCT p1 FROM Player p1, Player p2 " +
                "WHERE p1.salary > p2.salary AND p2.name = :name", Player.class);
        q.setParameter("name", name);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersBySalaryRange(double low, double high) {
        logger.info("getPlayersBySalaryRange");
        TypedQuery<Player> q = em.createQuery(
                "SELECT DISTINCT p FROM Player p WHERE p.salary BETWEEN :low AND :high", Player.class);
        q.setParameter("low", low);
        q.setParameter("high", high);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByLeagueId(String leagueId) {
        logger.info("getPlayersByLeagueId");
        TypedQuery<Player> q = em.createQuery(
                "SELECT DISTINCT p FROM Player p JOIN p.teams t JOIN t.league l WHERE l.id = :leagueId",
                Player.class);
        q.setParameter("leagueId", leagueId);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersBySport(String sport) {
        logger.info("getPlayersBySport");
        TypedQuery<Player> q = em.createQuery(
                "SELECT DISTINCT p FROM Player p JOIN p.teams t JOIN t.league l WHERE l.sport = :sport",
                Player.class);
        q.setParameter("sport", sport);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByCity(String city) {
        logger.info("getPlayersByCity");
        TypedQuery<Player> q = em.createQuery(
                "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.city = :city", Player.class);
        q.setParameter("city", city);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getAllPlayers() {
        logger.info("getAllPlayers");
        TypedQuery<Player> q = em.createQuery("SELECT p FROM Player p", Player.class);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersNotOnTeam() {
        logger.info("getPlayersNotOnTeam");
        TypedQuery<Player> q = em.createQuery(
                "SELECT DISTINCT p FROM Player p WHERE p.teams IS EMPTY", Player.class);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByPositionAndName(String position, String name) {
        logger.info("getPlayersByPositionAndName");
        TypedQuery<Player> q = em.createQuery(
                "SELECT DISTINCT p FROM Player p WHERE p.position = :position AND p.name = :name",
                Player.class);
        q.setParameter("position", position);
        q.setParameter("name", name);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<LeagueDetails> getLeaguesOfPlayer(String playerId) {
        logger.info("getLeaguesOfPlayer");
        TypedQuery<League> q = em.createQuery(
                "SELECT DISTINCT l FROM League l JOIN l.teams t JOIN t.players p WHERE p.id = :playerId",
                League.class);
        q.setParameter("playerId", playerId);
        List<League> leagues = q.getResultList();
        List<LeagueDetails> detailsList = new ArrayList<>();
        if (leagues == null) {
            logger.log(Level.WARNING, "No leagues found for player with ID {0}.", playerId);
            return null;
        }
        for (League league : leagues) {
            detailsList.add(new LeagueDetails(league.getId(), league.getName(), league.getSport()));
        }
        return detailsList;
    }

    @Transactional(readOnly = true)
    public List<String> getSportsOfPlayer(String playerId) {
        logger.info("getSportsOfPlayer");
        TypedQuery<String> q = em.createQuery(
                "SELECT DISTINCT l.sport FROM Player p JOIN p.teams t JOIN t.league l WHERE p.id = :playerId",
                String.class);
        q.setParameter("playerId", playerId);
        return q.getResultList();
    }

    public void createTeamInLeague(TeamDetails teamDetails, String leagueId) {
        logger.info("createTeamInLeague");
        League league = em.find(League.class, leagueId);
        if (league == null) {
            throw new IllegalArgumentException("League not found: " + leagueId);
        }
        Team team = new Team(teamDetails.getId(), teamDetails.getName(), teamDetails.getCity());
        em.persist(team);
        team.setLeague(league);
        league.addTeam(team);
    }

    public void removeTeam(String teamId) {
        logger.info("removeTeam");
        Team team = em.find(Team.class, teamId);
        Collection<Player> players = team.getPlayers();
        Iterator<Player> i = players.iterator();
        while (i.hasNext()) {
            Player player = i.next();
            player.dropTeam(team);
        }
        em.remove(team);
    }

    @Transactional(readOnly = true)
    public TeamDetails getTeam(String teamId) {
        logger.info("getTeam");
        Team team = em.find(Team.class, teamId);
        if (team == null) {
            return null;
        }
        return new TeamDetails(team.getId(), team.getName(), team.getCity());
    }

    public void createLeague(LeagueDetails leagueDetails) throws IncorrectSportException {
        logger.info("createLeague");
        if (leagueDetails.getSport().equalsIgnoreCase("soccer")
                || leagueDetails.getSport().equalsIgnoreCase("swimming")
                || leagueDetails.getSport().equalsIgnoreCase("basketball")
                || leagueDetails.getSport().equalsIgnoreCase("baseball")) {
            SummerLeague league = new SummerLeague(leagueDetails.getId(),
                    leagueDetails.getName(),
                    leagueDetails.getSport());
            em.persist(league);
        } else if (leagueDetails.getSport().equalsIgnoreCase("hockey")
                || leagueDetails.getSport().equalsIgnoreCase("skiing")
                || leagueDetails.getSport().equalsIgnoreCase("snowboarding")) {
            WinterLeague league = new WinterLeague(leagueDetails.getId(),
                    leagueDetails.getName(),
                    leagueDetails.getSport());
            em.persist(league);
        } else {
            throw new IncorrectSportException("The specified sport is not valid.");
        }
    }

    public void removeLeague(String leagueId) {
        logger.info("removeLeague");
        League league = em.find(League.class, leagueId);
        em.remove(league);
    }

    @Transactional(readOnly = true)
    public LeagueDetails getLeague(String leagueId) {
        logger.info("getLeague");
        League league = em.find(League.class, leagueId);
        if (league == null) {
            return null;
        }
        return new LeagueDetails(league.getId(), league.getName(), league.getSport());
    }

    private List<PlayerDetails> copyPlayersToDetails(List<Player> players) {
        List<PlayerDetails> detailsList = new ArrayList<>();
        for (Player player : players) {
            detailsList.add(new PlayerDetails(player.getId(),
                    player.getName(),
                    player.getPosition(),
                    player.getSalary()));
        }
        return detailsList;
    }
}
