package quarkus.tutorial.roster.request;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import quarkus.tutorial.roster.entity.League;
import quarkus.tutorial.roster.entity.Player;
import quarkus.tutorial.roster.entity.SummerLeague;
import quarkus.tutorial.roster.entity.Team;
import quarkus.tutorial.roster.entity.WinterLeague;
import quarkus.tutorial.roster.util.IncorrectSportException;
import quarkus.tutorial.roster.util.LeagueDetails;
import quarkus.tutorial.roster.util.PlayerDetails;
import quarkus.tutorial.roster.util.TeamDetails;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/roster")
public class RequestBean implements Request, Serializable {
    private static final Logger logger = Logger.getLogger("quarkus.tutorial.roster.request.RequestBean");

    @PersistenceContext
    private EntityManager em;
    private CriteriaBuilder cb;

    @PostConstruct
    private void init() {
        cb = em.getCriteriaBuilder();
    }

    @Override
    @PostMapping(value = "/player", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void createPlayer(
            @RequestParam("id") String id,
            @RequestParam("name") String name,
            @RequestParam("position") String position,
            @RequestParam("salary") double salary) {
        logger.info("createPlayer");
        try {
            Player player = new Player(id, name, position, salary);
            em.persist(player);
            em.flush();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @PostMapping(value = "/player/{playerId}/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void addPlayer(@PathVariable("playerId") String playerId, @PathVariable("teamId") String teamId) {
        logger.info("addPlayer");
        try {
            Player player = em.find(Player.class, playerId);
            Team team = em.find(Team.class, teamId);
            if (player == null || team == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player or Team not found");
            }
            team.addPlayer(player);
            player.addTeam(team);
            em.merge(player);
            em.merge(team);
            em.flush();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @DeleteMapping(value = "/player/{playerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void removePlayer(@PathVariable("playerId") String playerId) {
        logger.info("removePlayer");
        try {
            Player player = em.find(Player.class, playerId);
            if (player == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
            }
            Collection<Team> teams = player.getTeams();
            for (Team team : teams) {
                team.dropPlayer(player);
            }
            em.remove(player);
            em.flush();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @DeleteMapping(value = "/player/{playerId}/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void dropPlayer(@PathVariable("playerId") String playerId, @PathVariable("teamId") String teamId) {
        logger.info("dropPlayer");
        try {
            Player player = em.find(Player.class, playerId);
            Team team = em.find(Team.class, teamId);
            if (player == null || team == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player or Team not found");
            }
            team.dropPlayer(player);
            player.dropTeam(team);
            em.merge(player);
            em.merge(team);
            em.flush();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/player/{playerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PlayerDetails getPlayer(@PathVariable("playerId") String playerId) {
        logger.info("getPlayerDetails");
        try {
            Player player = em.find(Player.class, playerId);
            if (player == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
            }
            return new PlayerDetails(player.getId(), player.getName(), player.getPosition(), player.getSalary());
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/team/{teamId}/players", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersOfTeam(@PathVariable("teamId") String teamId) {
        logger.info("getPlayersOfTeam");
        try {
            Team team = em.find(Team.class, teamId);
            if (team == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found");
            }
            return copyPlayersToDetails(new ArrayList<>(team.getPlayers()));
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/league/{leagueId}/teams", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<TeamDetails> getTeamsOfLeague(@PathVariable("leagueId") String leagueId) {
        logger.info("getTeamsOfLeague");
        List<TeamDetails> detailsList = new ArrayList<>();
        try {
            League league = em.find(League.class, leagueId);
            if (league == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found");
            }
            for (Team team : league.getTeams()) {
                detailsList.add(new TeamDetails(team.getId(), team.getName(), team.getCity()));
            }
            return detailsList;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/players/position/{position}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByPosition(@PathVariable("position") String position) {
        logger.info("getPlayersByPosition");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            cq.where(cb.equal(player.get("position"), position));
            cq.select(player);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/players/salary/higher/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByHigherSalary(@PathVariable("name") String name) {
        logger.info("getPlayersByHigherSalary");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player1 = cq.from(Player.class);
            Root<Player> player2 = cq.from(Player.class);
            Predicate gtPredicate = cb.greaterThan(player1.get("salary"), player2.get("salary"));
            Predicate equalPredicate = cb.equal(player2.get("name"), name);
            cq.where(gtPredicate, equalPredicate);
            cq.select(player1).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/players/salary/range", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersBySalaryRange(@RequestParam("low") double low, @RequestParam("high") double high) {
        logger.info("getPlayersBySalaryRange");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            cq.where(cb.between(player.get("salary"), low, high));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/players/league/{leagueId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByLeagueId(@PathVariable("leagueId") String leagueId) {
        logger.info("getPlayersByLeagueId");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            Join<Player, Team> team = player.join("teams");
            Join<Team, League> league = team.join("league");
            cq.where(cb.equal(league.get("id"), leagueId));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/players/sport/{sport}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersBySport(@PathVariable("sport") String sport) {
        logger.info("getPlayersBySport");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            Join<Player, Team> team = player.join("teams");
            Join<Team, League> league = team.join("league");
            cq.where(cb.equal(league.get("sport"), sport));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/players/city/{city}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByCity(@PathVariable("city") String city) {
        logger.info("getPlayersByCity");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            Join<Player, Team> team = player.join("teams");
            cq.where(cb.equal(team.get("city"), city));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/players", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<PlayerDetails> getAllPlayers() {
        logger.info("getAllPlayers");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            cq.select(player);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/players/not-on-team", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersNotOnTeam() {
        logger.info("getPlayersNotOnTeam");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            cq.where(cb.isEmpty(player.get("teams")));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/players/position/{position}/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<PlayerDetails> getPlayersByPositionAndName(
            @PathVariable("position") String position,
            @PathVariable("name") String name) {
        logger.info("getPlayersByPositionAndName");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            cq.where(cb.equal(player.get("position"), position),
                    cb.equal(player.get("name"), name));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/player/{playerId}/leagues", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<LeagueDetails> getLeaguesOfPlayer(@PathVariable("playerId") String playerId) {
        logger.info("getLeaguesOfPlayer");
        List<LeagueDetails> detailsList = new ArrayList<>();
        try {
            CriteriaQuery<League> cq = cb.createQuery(League.class);
            Root<League> leagueRoot = cq.from(League.class);
            Join<League, Team> team = leagueRoot.join("teams");
            Join<Team, Player> player = team.join("players");
            cq.where(cb.equal(player.get("id"), playerId));
            cq.select(leagueRoot).distinct(true);
            TypedQuery<League> q = em.createQuery(cq);
            List<League> leagues = q.getResultList();
            if (leagues == null || leagues.isEmpty()) {
                logger.warning("No leagues found for player with ID " + playerId);
                return null;
            }
            for (League currentLeague : leagues) {
                detailsList.add(new LeagueDetails(currentLeague.getId(), currentLeague.getName(), currentLeague.getSport()));
            }
            return detailsList;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/player/{playerId}/sports", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<String> getSportsOfPlayer(@PathVariable("playerId") String playerId) {
        logger.info("getSportsOfPlayer");
        try {
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            Root<Player> player = cq.from(Player.class);
            Join<Player, Team> team = player.join("teams");
            Join<Team, League> league = team.join("league");
            cq.where(cb.equal(player.get("id"), playerId));
            cq.select(league.get("sport")).distinct(true);
            TypedQuery<String> q = em.createQuery(cq);
            return q.getResultList();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @PostMapping(value = "/team/league/{leagueId}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void createTeamInLeague(@RequestBody TeamDetails teamDetails, @PathVariable("leagueId") String leagueId) {
        logger.info("createTeamInLeague");
        try {
            League league = em.find(League.class, leagueId);
            if (league == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found");
            }
            Team team = new Team(teamDetails.getId(), teamDetails.getName(), teamDetails.getCity());
            em.persist(team);
            team.setLeague(league);
            league.addTeam(team);
            em.merge(league);
            em.flush();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @DeleteMapping(value = "/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void removeTeam(@PathVariable("teamId") String teamId) {
        logger.info("removeTeam");
        try {
            Team team = em.find(Team.class, teamId);
            if (team == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found");
            }
            for (Player player : team.getPlayers()) {
                player.dropTeam(team);
                em.merge(player);
            }
            em.remove(team);
            em.flush();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TeamDetails getTeam(@PathVariable("teamId") String teamId) {
        logger.info("getTeam");
        try {
            Team team = em.find(Team.class, teamId);
            if (team == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found");
            }
            return new TeamDetails(team.getId(), team.getName(), team.getCity());
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @PostMapping(value = "/league", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void createLeague(@RequestBody LeagueDetails leagueDetails) {
        logger.info("createLeague");
        try {
            String sport = leagueDetails.getSport().toLowerCase();
            if (sport.equals("soccer") || sport.equals("swimming") || sport.equals("basketball") || sport.equals("baseball")) {
                SummerLeague league = new SummerLeague(leagueDetails.getId(), leagueDetails.getName(), leagueDetails.getSport());
                em.persist(league);
                em.flush();
            } else if (sport.equals("hockey") || sport.equals("skiing") || sport.equals("snowboarding")) {
                WinterLeague league = new WinterLeague(leagueDetails.getId(), leagueDetails.getName(), leagueDetails.getSport());
                em.persist(league);
                em.flush();
            } else {
                throw new IncorrectSportException("The specified sport is not valid.");
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @Override
    @DeleteMapping(value = "/league/{leagueId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void removeLeague(@PathVariable("leagueId") String leagueId) {
        logger.info("removeLeague");
        try {
            League league = em.find(League.class, leagueId);
            if (league == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found");
            }
            em.remove(league);
            em.flush();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    @GetMapping(value = "/league/{leagueId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public LeagueDetails getLeague(@PathVariable("leagueId") String leagueId) {
        logger.info("getLeague");
        try {
            League league = em.find(League.class, leagueId);
            if (league == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found");
            }
            return new LeagueDetails(league.getId(), league.getName(), league.getSport());
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    private List<PlayerDetails> copyPlayersToDetails(List<Player> players) {
        List<PlayerDetails> detailsList = new ArrayList<>();
        for (Player player : players) {
            detailsList.add(new PlayerDetails(player.getId(), player.getName(), player.getPosition(), player.getSalary()));
        }
        return detailsList;
    }
}
