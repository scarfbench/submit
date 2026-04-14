package roster.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import roster.entity.League;
import roster.entity.Player;
import roster.entity.SummerLeague;
import roster.entity.Team;
import roster.entity.WinterLeague;
import roster.util.IncorrectSportException;
import roster.util.LeagueDetails;
import roster.util.PlayerDetails;
import roster.util.TeamDetails;

@Service
@Transactional
public class RequestService {

    private static final Logger logger = Logger.getLogger("roster.service.RequestService");

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
        return new PlayerDetails(player.getId(), player.getName(),
                player.getPosition(), player.getSalary());
    }

    public List<PlayerDetails> getPlayersOfTeam(String teamId) {
        logger.info("getPlayersOfTeam");
        Team team = em.find(Team.class, teamId);
        return copyPlayersToDetails(new ArrayList<>(team.getPlayers()));
    }

    @Transactional(readOnly = true)
    public List<LeagueDetails> getAllLeagues() {
        logger.info("getAllLeagues");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<League> cq = cb.createQuery(League.class);
        Root<League> league = cq.from(League.class);
        cq.select(league);
        cq.orderBy(cb.asc(league.get("id")));
        List<League> leagues = em.createQuery(cq).getResultList();

        List<LeagueDetails> details = new ArrayList<>();
        for (League l : leagues) {
            details.add(new LeagueDetails(l.getId(), l.getName(), l.getSport()));
        }
        return details;
    }

    @Transactional(readOnly = true)
    public List<TeamDetails> getTeamsOfLeague(String leagueId) {
        logger.info("getTeamsOfLeague");
        List<TeamDetails> detailsList = new ArrayList<>();
        League league = em.find(League.class, leagueId);
        if (league == null) {
            return detailsList;
        }
        Collection<Team> teams = league.getTeams();
        for (Team team : teams) {
            detailsList.add(new TeamDetails(team.getId(), team.getName(), team.getCity()));
        }
        return detailsList;
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByPosition(String position) {
        logger.info("getPlayersByPosition");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        cq.where(cb.equal(player.get("position"), position));
        cq.select(player);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByHigherSalary(String name) {
        logger.info("getPlayersByHigherSalary");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player1 = cq.from(Player.class);
        Root<Player> player2 = cq.from(Player.class);

        Predicate gtPredicate = cb.greaterThan(
                player1.get("salary"),
                player2.get("salary"));
        Predicate equalPredicate = cb.equal(
                player2.get("name"),
                name);
        cq.where(gtPredicate, equalPredicate);
        cq.select(player1).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersBySalaryRange(double low, double high) {
        logger.info("getPlayersBySalaryRange");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        cq.where(cb.between(player.get("salary"), low, high));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByLeagueId(String leagueId) {
        logger.info("getPlayersByLeagueId");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        Join<Player, Team> team = player.join("teams");
        Join<Team, League> league = team.join("league");
        cq.where(cb.equal(league.get("id"), leagueId));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersBySport(String sport) {
        logger.info("getPlayersBySport");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        Join<Player, Team> team = player.join("teams");
        Join<Team, League> league = team.join("league");
        cq.where(cb.equal(league.get("sport"), sport));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByCity(String city) {
        logger.info("getPlayersByCity");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        Join<Player, Team> team = player.join("teams");
        cq.where(cb.equal(team.get("city"), city));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getAllPlayers() {
        logger.info("getAllPlayers");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        cq.select(player);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersNotOnTeam() {
        logger.info("getPlayersNotOnTeam");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        cq.where(cb.isEmpty(player.get("teams")));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByPositionAndName(String position, String name) {
        logger.info("getPlayersByPositionAndName");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        cq.where(cb.equal(player.get("position"), position),
                cb.equal(player.get("name"), name));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Transactional(readOnly = true)
    public List<LeagueDetails> getLeaguesOfPlayer(String playerId) {
        logger.info("getLeaguesOfPlayer");
        List<LeagueDetails> detailsList = new ArrayList<>();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<League> cq = cb.createQuery(League.class);
        Root<League> league = cq.from(League.class);
        Join<League, Team> team = league.join("teams");
        Join<Team, Player> player = team.join("players");
        cq.where(cb.equal(player.get("id"), playerId));
        cq.select(league).distinct(true);
        TypedQuery<League> q = em.createQuery(cq);
        List<League> leagues = q.getResultList();

        if (leagues == null) {
            logger.log(Level.WARNING, "No leagues found for player with ID {0}.", playerId);
            return null;
        }
        for (League l : leagues) {
            detailsList.add(new LeagueDetails(l.getId(), l.getName(), l.getSport()));
        }
        return detailsList;
    }

    @Transactional(readOnly = true)
    public List<String> getSportsOfPlayer(String playerId) {
        logger.info("getSportsOfPlayer");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Player> player = cq.from(Player.class);
        Join<Player, Team> team = player.join("teams");
        Join<Team, League> league = team.join("league");
        cq.where(cb.equal(player.get("id"), playerId));
        cq.select(league.get("sport")).distinct(true);
        TypedQuery<String> q = em.createQuery(cq);
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
        if (team == null) {
            throw new RuntimeException("Team not found: " + teamId);
        }
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
        if (league != null) {
            em.remove(league);
        }
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
