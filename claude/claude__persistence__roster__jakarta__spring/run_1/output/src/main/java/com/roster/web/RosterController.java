package com.roster.web;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.roster.service.RosterService;
import com.roster.util.IncorrectSportException;
import com.roster.util.LeagueDetails;
import com.roster.util.PlayerDetails;
import com.roster.util.TeamDetails;

@RestController
@RequestMapping("/roster")
public class RosterController {

    private static final Logger logger = Logger.getLogger("roster.web.RosterController");

    private final RosterService rosterService;

    public RosterController(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    // -----------------------------------------------------------------------
    // League endpoints
    // -----------------------------------------------------------------------

    @PostMapping("/league")
    public ResponseEntity<?> createLeague(@RequestBody LeagueDetails league) {
        try {
            rosterService.createLeague(league);
            return ResponseEntity.ok().build();
        } catch (IncorrectSportException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Invalid sport: " + league.getSport() + "\"}");
        } catch (Exception e) {
            if (hasIncorrectSportCause(e)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"Invalid sport: " + league.getSport() + "\"}");
            }
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/league/{id}")
    public ResponseEntity<?> getLeague(@PathVariable("id") String id) {
        LeagueDetails league = rosterService.getLeague(id);
        if (league == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(league);
    }

    @DeleteMapping("/league/{id}")
    public ResponseEntity<?> removeLeague(@PathVariable("id") String id) {
        rosterService.removeLeague(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/league/{id}/teams")
    public List<TeamDetails> getTeamsOfLeague(@PathVariable("id") String id) {
        return rosterService.getTeamsOfLeague(id);
    }

    // -----------------------------------------------------------------------
    // Team endpoints
    // -----------------------------------------------------------------------

    @PostMapping("/team/league/{leagueId}")
    public ResponseEntity<?> createTeamInLeague(@RequestBody TeamDetails team,
                                                 @PathVariable("leagueId") String leagueId) {
        rosterService.createTeamInLeague(team, leagueId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/team/{id}")
    public ResponseEntity<?> getTeam(@PathVariable("id") String id) {
        TeamDetails team = rosterService.getTeam(id);
        if (team == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(team);
    }

    @DeleteMapping("/team/{id}")
    public ResponseEntity<?> removeTeam(@PathVariable("id") String id) {
        rosterService.removeTeam(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/team/{id}/players")
    public List<PlayerDetails> getPlayersOfTeam(@PathVariable("id") String id) {
        return rosterService.getPlayersOfTeam(id);
    }

    // -----------------------------------------------------------------------
    // Player endpoints
    // -----------------------------------------------------------------------

    @PostMapping("/player")
    public ResponseEntity<?> createPlayer(
            @RequestParam("id") String id,
            @RequestParam("name") String name,
            @RequestParam("position") String position,
            @RequestParam("salary") double salary) {
        rosterService.createPlayer(id, name, position, salary);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/player/{id}")
    public ResponseEntity<?> getPlayer(@PathVariable("id") String id) {
        try {
            PlayerDetails player = rosterService.getPlayer(id);
            if (player == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(player);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/player/{id}")
    public ResponseEntity<?> removePlayer(@PathVariable("id") String id) {
        try {
            rosterService.removePlayer(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/player/{pid}/team/{tid}")
    public ResponseEntity<?> addPlayerToTeam(@PathVariable("pid") String pid,
                                              @PathVariable("tid") String tid) {
        rosterService.addPlayer(pid, tid);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/player/{pid}/team/{tid}")
    public ResponseEntity<?> dropPlayerFromTeam(@PathVariable("pid") String pid,
                                                 @PathVariable("tid") String tid) {
        rosterService.dropPlayer(pid, tid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/player/{id}/leagues")
    public List<LeagueDetails> getLeaguesOfPlayer(@PathVariable("id") String id) {
        return rosterService.getLeaguesOfPlayer(id);
    }

    @GetMapping("/player/{id}/sports")
    public List<String> getSportsOfPlayer(@PathVariable("id") String id) {
        return rosterService.getSportsOfPlayer(id);
    }

    // -----------------------------------------------------------------------
    // Query endpoints
    // -----------------------------------------------------------------------

    @GetMapping("/players")
    public List<PlayerDetails> getAllPlayers() {
        return rosterService.getAllPlayers();
    }

    @GetMapping("/leagues")
    public List<LeagueDetails> getAllLeagues() {
        return rosterService.getAllLeagues();
    }

    @GetMapping("/players/position/{position}")
    public List<PlayerDetails> getPlayersByPosition(@PathVariable("position") String position) {
        return rosterService.getPlayersByPosition(position);
    }

    @GetMapping("/players/salary/higher/{name}")
    public List<PlayerDetails> getPlayersByHigherSalary(@PathVariable("name") String name) {
        return rosterService.getPlayersByHigherSalary(name);
    }

    @GetMapping("/players/salary/range")
    public List<PlayerDetails> getPlayersBySalaryRange(
            @RequestParam("low") double low,
            @RequestParam("high") double high) {
        return rosterService.getPlayersBySalaryRange(low, high);
    }

    @GetMapping("/players/league/{id}")
    public List<PlayerDetails> getPlayersByLeague(@PathVariable("id") String id) {
        return rosterService.getPlayersByLeagueId(id);
    }

    @GetMapping("/players/sport/{sport}")
    public List<PlayerDetails> getPlayersBySport(@PathVariable("sport") String sport) {
        return rosterService.getPlayersBySport(sport);
    }

    @GetMapping("/players/city/{city}")
    public List<PlayerDetails> getPlayersByCity(@PathVariable("city") String city) {
        return rosterService.getPlayersByCity(city);
    }

    @GetMapping("/players/not-on-team")
    public List<PlayerDetails> getPlayersNotOnTeam() {
        return rosterService.getPlayersNotOnTeam();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private boolean hasIncorrectSportCause(Exception e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof IncorrectSportException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
