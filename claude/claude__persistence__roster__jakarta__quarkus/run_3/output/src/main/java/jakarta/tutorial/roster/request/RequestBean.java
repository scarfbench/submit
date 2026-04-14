/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.roster.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import jakarta.tutorial.roster.util.IncorrectSportException;
import jakarta.tutorial.roster.util.LeagueDetails;
import jakarta.tutorial.roster.util.PlayerDetails;
import jakarta.tutorial.roster.util.TeamDetails;
import jakartaee.tutorial.roster.entity.League;
import jakartaee.tutorial.roster.entity.League_;
import jakartaee.tutorial.roster.entity.Player;
import jakartaee.tutorial.roster.entity.Player_;
import jakartaee.tutorial.roster.entity.SummerLeague;
import jakartaee.tutorial.roster.entity.Team;
import jakartaee.tutorial.roster.entity.Team_;
import jakartaee.tutorial.roster.entity.WinterLeague;

/**
 * CDI bean replacing the former EJB RequestBean.
 * Migrated from @Stateful @LocalBean to @ApplicationScoped @Transactional.
 */
@ApplicationScoped
@Transactional
public class RequestBean implements Request {

    private static final Logger logger = Logger.getLogger("roster.request.RequestBean");

    @PersistenceContext
    EntityManager em;

    private CriteriaBuilder cb() {
        return em.getCriteriaBuilder();
    }

    @Override
    public void createPlayer(String id,
            String name,
            String position,
            double salary) {
        logger.info("createPlayer");
        Player player = new Player(id, name, position, salary);
        em.persist(player);
    }

    @Override
    public void addPlayer(String playerId, String teamId) {
        logger.info("addPlayer");
        Player player = em.find(Player.class, playerId);
        Team team = em.find(Team.class, teamId);
        team.addPlayer(player);
        player.addTeam(team);
    }

    @Override
    public void removePlayer(String playerId) {
        logger.info("removePlayer");
        Player player = em.find(Player.class, playerId);
        Collection<Team> teams = player.getTeams();
        Iterator<Team> i = teams.iterator();
        while (i.hasNext()) {
            Team team = i.next();
            team.dropPlayer(player);
        }
        em.remove(player);
    }

    @Override
    public void dropPlayer(String playerId, String teamId) {
        logger.info("dropPlayer");
        Player player = em.find(Player.class, playerId);
        Team team = em.find(Team.class, teamId);
        team.dropPlayer(player);
        player.dropTeam(team);
    }

    @Override
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

    @Override
    public List<PlayerDetails> getPlayersOfTeam(String teamId) {
        logger.info("getPlayersOfTeam");
        Team team = em.find(Team.class, teamId);
        return this.copyPlayersToDetails(new ArrayList<>(team.getPlayers()));
    }

    @Override
    public List<LeagueDetails> getAllLeagues() {
        logger.info("getAllLeagues");
        CriteriaBuilder cb = cb();
        CriteriaQuery<League> cq = cb.createQuery(League.class);
        Root<League> league = cq.from(League.class);
        cq.select(league);
        cq.orderBy(cb.asc(league.get(League_.id)));
        List<League> leagues = em.createQuery(cq).getResultList();

        List<LeagueDetails> details = new ArrayList<>();
        for (League l : leagues) {
            details.add(new LeagueDetails(l.getId(), l.getName(), l.getSport()));
        }
        return details;
    }

    @Override
    public List<TeamDetails> getTeamsOfLeague(String leagueId) {
        logger.info("getTeamsOfLeague");
        List<TeamDetails> detailsList = new ArrayList<>();

        League league = em.find(League.class, leagueId);
        Collection<Team> teams = league.getTeams();

        Iterator<Team> i = teams.iterator();
        while (i.hasNext()) {
            Team team = (Team) i.next();
            TeamDetails teamDetails = new TeamDetails(team.getId(),
                    team.getName(),
                    team.getCity());
            detailsList.add(teamDetails);
        }
        return detailsList;
    }

    @Override
    public List<PlayerDetails> getPlayersByPosition(String position) {
        logger.info("getPlayersByPosition");
        CriteriaBuilder cb = cb();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        cq.where(cb.equal(player.get(Player_.position), position));
        cq.select(player);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Override
    public List<PlayerDetails> getPlayersByHigherSalary(String name) {
        logger.info("getPlayersByHigherSalary");
        CriteriaBuilder cb = cb();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player1 = cq.from(Player.class);
        Root<Player> player2 = cq.from(Player.class);

        Predicate gtPredicate = cb.greaterThan(
                player1.get(Player_.salary),
                player2.get(Player_.salary));
        Predicate equalPredicate = cb.equal(
                player2.get(Player_.name),
                name);
        cq.where(gtPredicate, equalPredicate);
        cq.select(player1).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Override
    public List<PlayerDetails> getPlayersBySalaryRange(double low, double high) {
        logger.info("getPlayersBySalaryRange");
        CriteriaBuilder cb = cb();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        cq.where(cb.between(player.get(Player_.salary), low, high));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Override
    public List<PlayerDetails> getPlayersByLeagueId(String leagueId) {
        logger.info("getPlayersByLeagueId");
        CriteriaBuilder cb = cb();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        Join<Player, Team> team = player.join(Player_.teams);
        Join<Team, League> league = team.join(Team_.league);
        cq.where(cb.equal(league.get(League_.id), leagueId));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Override
    public List<PlayerDetails> getPlayersBySport(String sport) {
        logger.info("getPlayersBySport");
        CriteriaBuilder cb = cb();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        Join<Player, Team> team = player.join(Player_.teams);
        Join<Team, League> league = team.join(Team_.league);
        cq.where(cb.equal(league.get(League_.sport), sport));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Override
    public List<PlayerDetails> getPlayersByCity(String city) {
        logger.info("getPlayersByCity");
        CriteriaBuilder cb = cb();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        Join<Player, Team> team = player.join(Player_.teams);
        cq.where(cb.equal(team.get(Team_.city), city));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Override
    public List<PlayerDetails> getAllPlayers() {
        logger.info("getAllPlayers");
        CriteriaBuilder cb = cb();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        cq.select(player);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Override
    public List<PlayerDetails> getPlayersNotOnTeam() {
        logger.info("getPlayersNotOnTeam");
        CriteriaBuilder cb = cb();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        cq.where(cb.isEmpty(player.get(Player_.teams)));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Override
    public List<PlayerDetails> getPlayersByPositionAndName(String position, String name) {
        logger.info("getPlayersByPositionAndName");
        CriteriaBuilder cb = cb();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> player = cq.from(Player.class);
        cq.where(cb.equal(player.get(Player_.position), position),
                cb.equal(player.get(Player_.name), name));
        cq.select(player).distinct(true);
        TypedQuery<Player> q = em.createQuery(cq);
        return copyPlayersToDetails(q.getResultList());
    }

    @Override
    public List<LeagueDetails> getLeaguesOfPlayer(String playerId) {
        logger.info("getLeaguesOfPlayer");
        List<LeagueDetails> detailsList = new ArrayList<>();

        CriteriaBuilder cb = cb();
        CriteriaQuery<League> cq = cb.createQuery(League.class);
        Root<League> league = cq.from(League.class);
        Join<League, Team> team = league.join(League_.teams);
        Join<Team, Player> player = team.join(Team_.players);
        cq.where(cb.equal(player.get(Player_.id), playerId));
        cq.select(league).distinct(true);
        TypedQuery<League> q = em.createQuery(cq);
        List<League> leagues = q.getResultList();

        if (leagues == null || leagues.isEmpty()) {
            logger.log(Level.WARNING, "No leagues found for player with ID {0}.", playerId);
            return detailsList;
        }

        for (League l : leagues) {
            detailsList.add(new LeagueDetails(l.getId(), l.getName(), l.getSport()));
        }
        return detailsList;
    }

    @Override
    public List<String> getSportsOfPlayer(String playerId) {
        logger.info("getSportsOfPlayer");
        CriteriaBuilder cb = cb();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Player> player = cq.from(Player.class);
        Join<Player, Team> team = player.join(Player_.teams);
        Join<Team, League> league = team.join(Team_.league);
        cq.where(cb.equal(player.get(Player_.id), playerId));
        cq.select(league.get(League_.sport)).distinct(true);
        TypedQuery<String> q = em.createQuery(cq);
        return q.getResultList();
    }

    @Override
    public void createTeamInLeague(TeamDetails teamDetails, String leagueId) {
        logger.info("createTeamInLeague");
        League league = em.find(League.class, leagueId);
        if (league == null) {
            throw new IllegalArgumentException("League not found: " + leagueId);
        }
        Team team = new Team(teamDetails.getId(),
                teamDetails.getName(),
                teamDetails.getCity());
        em.persist(team);
        team.setLeague(league);
        league.addTeam(team);
    }

    @Override
    public void removeTeam(String teamId) {
        logger.info("removeTeam");
        Team team = em.find(Team.class, teamId);
        Collection<Player> players = team.getPlayers();
        Iterator<Player> i = players.iterator();
        while (i.hasNext()) {
            Player player = (Player) i.next();
            player.dropTeam(team);
        }
        em.remove(team);
    }

    @Override
    public TeamDetails getTeam(String teamId) {
        logger.info("getTeam");
        Team team = em.find(Team.class, teamId);
        if (team == null) {
            return null;
        }
        return new TeamDetails(team.getId(), team.getName(), team.getCity());
    }

    @Override
    public void createLeague(LeagueDetails leagueDetails) {
        logger.info("createLeague");
        try {
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
        } catch (IncorrectSportException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void removeLeague(String leagueId) {
        logger.info("removeLeague");
        League league = em.find(League.class, leagueId);
        em.remove(league);
    }

    @Override
    public LeagueDetails getLeague(String leagueId) {
        logger.info("getLeague");
        League league = em.find(League.class, leagueId);
        if (league == null) {
            return null;
        }
        return new LeagueDetails(league.getId(),
                league.getName(),
                league.getSport());
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
