package roster.web;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import roster.service.RequestService;
import roster.util.IncorrectSportException;
import roster.util.LeagueDetails;
import roster.util.PlayerDetails;
import roster.util.TeamDetails;

@RestController
public class RosterController {

    private static final Logger logger = Logger.getLogger("roster.web.RosterController");

    private final RequestService requestService;

    public RosterController(RequestService requestService) {
        this.requestService = requestService;
    }

    // -----------------------------------------------------------------------
    // League endpoints
    // -----------------------------------------------------------------------

    @PostMapping(value = "/league", consumes = "application/json")
    public ResponseEntity<?> createLeague(@RequestBody LeagueDetails league) {
        try {
            requestService.createLeague(league);
            return ResponseEntity.ok().build();
        } catch (IncorrectSportException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid sport: " + league.getSport()));
        } catch (Exception e) {
            if (hasIncorrectSportCause(e)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid sport: " + league.getSport()));
            }
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/league/{id}")
    public ResponseEntity<LeagueDetails> getLeague(@PathVariable String id) {
        LeagueDetails league = requestService.getLeague(id);
        if (league == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(league);
    }

    @DeleteMapping("/league/{id}")
    public ResponseEntity<Void> removeLeague(@PathVariable String id) {
        requestService.removeLeague(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/league/{id}/teams")
    public List<TeamDetails> getTeamsOfLeague(@PathVariable String id) {
        return requestService.getTeamsOfLeague(id);
    }

    // -----------------------------------------------------------------------
    // Team endpoints
    // -----------------------------------------------------------------------

    @PostMapping(value = "/team/league/{leagueId}", consumes = "application/json")
    public ResponseEntity<Void> createTeamInLeague(@RequestBody TeamDetails team,
            @PathVariable String leagueId) {
        requestService.createTeamInLeague(team, leagueId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/team/{id}")
    public ResponseEntity<TeamDetails> getTeam(@PathVariable String id) {
        TeamDetails team = requestService.getTeam(id);
        if (team == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(team);
    }

    @DeleteMapping("/team/{id}")
    public ResponseEntity<Void> removeTeam(@PathVariable String id) {
        requestService.removeTeam(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/team/{id}/players")
    public List<PlayerDetails> getPlayersOfTeam(@PathVariable String id) {
        return requestService.getPlayersOfTeam(id);
    }

    // -----------------------------------------------------------------------
    // Player endpoints
    // -----------------------------------------------------------------------

    @PostMapping("/player")
    public ResponseEntity<Void> createPlayer(
            @RequestParam("id") String id,
            @RequestParam("name") String name,
            @RequestParam("position") String position,
            @RequestParam("salary") double salary) {
        requestService.createPlayer(id, name, position, salary);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/player/{id}")
    public ResponseEntity<PlayerDetails> getPlayer(@PathVariable String id) {
        try {
            PlayerDetails player = requestService.getPlayer(id);
            if (player == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(player);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/player/{id}")
    public ResponseEntity<Void> removePlayer(@PathVariable String id) {
        try {
            requestService.removePlayer(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/player/{pid}/team/{tid}")
    public ResponseEntity<Void> addPlayerToTeam(@PathVariable String pid, @PathVariable String tid) {
        requestService.addPlayer(pid, tid);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/player/{pid}/team/{tid}")
    public ResponseEntity<Void> dropPlayerFromTeam(@PathVariable String pid, @PathVariable String tid) {
        requestService.dropPlayer(pid, tid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/player/{id}/leagues")
    public List<LeagueDetails> getLeaguesOfPlayer(@PathVariable String id) {
        return requestService.getLeaguesOfPlayer(id);
    }

    @GetMapping("/player/{id}/sports")
    public List<String> getSportsOfPlayer(@PathVariable String id) {
        return requestService.getSportsOfPlayer(id);
    }

    // -----------------------------------------------------------------------
    // Query endpoints
    // -----------------------------------------------------------------------

    @GetMapping("/players")
    public List<PlayerDetails> getAllPlayers() {
        return requestService.getAllPlayers();
    }

    @GetMapping("/players/position/{position}")
    public List<PlayerDetails> getPlayersByPosition(@PathVariable String position) {
        return requestService.getPlayersByPosition(position);
    }

    @GetMapping("/players/salary/higher/{name}")
    public List<PlayerDetails> getPlayersByHigherSalary(@PathVariable String name) {
        return requestService.getPlayersByHigherSalary(name);
    }

    @GetMapping("/players/salary/range")
    public List<PlayerDetails> getPlayersBySalaryRange(
            @RequestParam("low") double low,
            @RequestParam("high") double high) {
        return requestService.getPlayersBySalaryRange(low, high);
    }

    @GetMapping("/players/league/{id}")
    public List<PlayerDetails> getPlayersByLeague(@PathVariable String id) {
        return requestService.getPlayersByLeagueId(id);
    }

    @GetMapping("/players/sport/{sport}")
    public List<PlayerDetails> getPlayersBySport(@PathVariable String sport) {
        return requestService.getPlayersBySport(sport);
    }

    @GetMapping("/players/city/{city}")
    public List<PlayerDetails> getPlayersByCity(@PathVariable String city) {
        return requestService.getPlayersByCity(city);
    }

    @GetMapping("/players/not-on-team")
    public List<PlayerDetails> getPlayersNotOnTeam() {
        return requestService.getPlayersNotOnTeam();
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
