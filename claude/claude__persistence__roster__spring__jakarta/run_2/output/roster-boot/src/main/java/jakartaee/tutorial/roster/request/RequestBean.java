package jakartaee.tutorial.roster.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import jakartaee.tutorial.roster.entity.League;
import jakartaee.tutorial.roster.entity.Player;
import jakartaee.tutorial.roster.entity.SummerLeague;
import jakartaee.tutorial.roster.entity.Team;
import jakartaee.tutorial.roster.entity.WinterLeague;
import jakartaee.tutorial.roster.util.IncorrectSportException;
import jakartaee.tutorial.roster.util.LeagueDetails;
import jakartaee.tutorial.roster.util.PlayerDetails;
import jakartaee.tutorial.roster.util.TeamDetails;

@Named("requestBean")
@ApplicationScoped
@Transactional
public class RequestBean implements Request, Serializable {

  @PersistenceContext(unitName = "roster-pu")
  private EntityManager em;

  @PostConstruct
  private void init() {
  }

  @Override
  public void createPlayer(String id, String name, String position, double salary) {
    Player p = new Player(id, name, position, salary);
    em.persist(p);
  }

  @Override
  public void addPlayer(String playerId, String teamId) {
    Player p = em.find(Player.class, playerId);
    Team t = em.find(Team.class, teamId);
    if (p == null || t == null) return;
    t.addPlayer(p);
    em.merge(t);
  }

  @Override
  public void removePlayer(String playerId) {
    Player p = em.find(Player.class, playerId);
    if (p == null) return;
    List<Team> teams = new ArrayList<>(Optional.ofNullable((List<Team>) new ArrayList<>(p.getTeams())).orElse(List.of()));
    for (Team t : teams) {
      t.dropPlayer(p);
      em.merge(t);
    }
    // Re-find after modifications
    p = em.find(Player.class, playerId);
    if (p != null) {
      em.remove(p);
    }
  }

  @Override
  public void dropPlayer(String playerId, String teamId) {
    Player p = em.find(Player.class, playerId);
    Team t = em.find(Team.class, teamId);
    if (p == null || t == null) return;
    t.dropPlayer(p);
    em.merge(t);
  }

  @Override
  public PlayerDetails getPlayer(String playerId) {
    Player p = em.find(Player.class, playerId);
    return p != null ? toPlayerDetails(p) : null;
  }

  @Override
  public List<PlayerDetails> getPlayersOfTeam(String teamId) {
    TypedQuery<Player> q = em.createQuery(
        "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.id = :teamId", Player.class);
    q.setParameter("teamId", teamId);
    return q.getResultList().stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<TeamDetails> getTeamsOfLeague(String leagueId) {
    TypedQuery<Team> q = em.createQuery(
        "SELECT t FROM Team t WHERE t.league.id = :leagueId", Team.class);
    q.setParameter("leagueId", leagueId);
    return q.getResultList().stream().map(this::toTeamDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersByPosition(String position) {
    TypedQuery<Player> q = em.createQuery(
        "SELECT p FROM Player p WHERE p.position = :position", Player.class);
    q.setParameter("position", position);
    return q.getResultList().stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersByHigherSalary(String name) {
    TypedQuery<Player> q = em.createQuery(
        "SELECT p FROM Player p WHERE p.name = :name", Player.class);
    q.setParameter("name", name);
    q.setMaxResults(1);
    List<Player> baseList = q.getResultList();
    if (baseList.isEmpty()) return List.of();
    double baseSalary = baseList.get(0).getSalary();
    TypedQuery<Player> q2 = em.createQuery(
        "SELECT p FROM Player p WHERE p.salary > :salary", Player.class);
    q2.setParameter("salary", baseSalary);
    return q2.getResultList().stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersBySalaryRange(double low, double high) {
    TypedQuery<Player> q = em.createQuery(
        "SELECT p FROM Player p WHERE p.salary BETWEEN :low AND :high", Player.class);
    q.setParameter("low", low);
    q.setParameter("high", high);
    return q.getResultList().stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersByLeagueId(String leagueId) {
    TypedQuery<Player> q = em.createQuery(
        "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.id = :leagueId", Player.class);
    q.setParameter("leagueId", leagueId);
    return q.getResultList().stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersBySport(String sport) {
    TypedQuery<Player> q = em.createQuery(
        "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.sport = :sport", Player.class);
    q.setParameter("sport", sport);
    return q.getResultList().stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersByCity(String city) {
    TypedQuery<Player> q = em.createQuery(
        "SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.city = :city", Player.class);
    q.setParameter("city", city);
    return q.getResultList().stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getAllPlayers() {
    TypedQuery<Player> q = em.createQuery(
        "SELECT p FROM Player p ORDER BY p.id", Player.class);
    return q.getResultList().stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersNotOnTeam() {
    TypedQuery<Player> q = em.createQuery(
        "SELECT p FROM Player p WHERE p.teams IS EMPTY", Player.class);
    return q.getResultList().stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersByPositionAndName(String position, String name) {
    TypedQuery<Player> q = em.createQuery(
        "SELECT p FROM Player p WHERE p.position = :position AND p.name = :name", Player.class);
    q.setParameter("position", position);
    q.setParameter("name", name);
    return q.getResultList().stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<LeagueDetails> getLeaguesOfPlayer(String playerId) {
    Player p = em.find(Player.class, playerId);
    if (p == null) return List.of();
    return Optional.ofNullable(p.getTeams()).map(teams -> new ArrayList<>(teams)).orElse(new ArrayList<>()).stream()
        .map(Team::getLeague).filter(Objects::nonNull)
        .collect(Collectors.toMap(League::getId, l -> l, (a, b) -> a)).values().stream()
        .map(this::toLeagueDetails).toList();
  }

  @Override
  public List<String> getSportsOfPlayer(String playerId) {
    Player p = em.find(Player.class, playerId);
    if (p == null) return List.of();
    return Optional.ofNullable(p.getTeams()).map(teams -> new ArrayList<>(teams)).orElse(new ArrayList<>()).stream()
        .map(Team::getLeague).filter(Objects::nonNull).map(League::getSport).filter(Objects::nonNull)
        .collect(Collectors.toCollection(java.util.LinkedHashSet::new)).stream().toList();
  }

  @Override
  public void createTeamInLeague(TeamDetails teamDetails, String leagueId) {
    League league = em.find(League.class, leagueId);
    if (league == null) return;
    Team t = new Team(teamDetails.getId(), teamDetails.getName(), teamDetails.getCity());
    t.setLeague(league);
    em.persist(t);
  }

  @Override
  public void removeTeam(String teamId) {
    Team t = em.find(Team.class, teamId);
    if (t == null) return;
    List<Player> players = new ArrayList<>(Optional.ofNullable(t.getPlayers()).map(c -> new ArrayList<>(c)).orElse(new ArrayList<>()));
    for (Player p : players) {
      t.dropPlayer(p);
    }
    em.merge(t);
    t = em.find(Team.class, teamId);
    if (t != null) {
      em.remove(t);
    }
  }

  @Override
  public TeamDetails getTeam(String teamId) {
    Team t = em.find(Team.class, teamId);
    return t != null ? toTeamDetails(t) : null;
  }

  @Override
  public void createLeague(LeagueDetails leagueDetails) {
    League l = instantiateLeague(leagueDetails);
    em.persist(l);
  }

  @Override
  public void removeLeague(String leagueId) {
    League l = em.find(League.class, leagueId);
    if (l != null) {
      em.remove(l);
    }
  }

  @Override
  public LeagueDetails getLeague(String leagueId) {
    League l = em.find(League.class, leagueId);
    return l != null ? toLeagueDetails(l) : null;
  }

  private PlayerDetails toPlayerDetails(Player p) {
    return new PlayerDetails(p.getId(), p.getName(), p.getPosition(), p.getSalary());
  }

  private TeamDetails toTeamDetails(Team t) {
    return new TeamDetails(t.getId(), t.getName(), t.getCity());
  }

  private LeagueDetails toLeagueDetails(League l) {
    return new LeagueDetails(l.getId(), l.getName(), l.getSport());
  }

  private League instantiateLeague(LeagueDetails d) {
    try {
      String sport = d.getSport() == null ? "" : d.getSport().toLowerCase();
      if (sport.contains("ski") || sport.contains("hockey") || sport.contains("ice") || sport.contains("snow")) {
        return new WinterLeague(d.getId(), d.getName(), d.getSport());
      }
      return new SummerLeague(d.getId(), d.getName(), d.getSport());
    } catch (IncorrectSportException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
