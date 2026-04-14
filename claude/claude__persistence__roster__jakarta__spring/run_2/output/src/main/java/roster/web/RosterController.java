package roster.web;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import roster.service.RosterService;
import roster.util.IncorrectSportException;
import roster.util.LeagueDetails;
import roster.util.PlayerDetails;
import roster.util.TeamDetails;

@RestController
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
            return ResponseEntity.badRequest()
                    .body("{\"error\":\"Invalid sport: " + league.getSport() + "\"}");
        }
    }

    @GetMapping("/league/{id}")
    public ResponseEntity<LeagueDetails> getLeague(@PathVariable String id) {
        LeagueDetails league = rosterService.getLeague(id);
        if (league == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(league);
    }

    @DeleteMapping("/league/{id}")
    public ResponseEntity<Void> removeLeague(@PathVariable String id) {
        rosterService.removeLeague(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/league/{id}/teams")
    public List<TeamDetails> getTeamsOfLeague(@PathVariable String id) {
        return rosterService.getTeamsOfLeague(id);
    }

    // -----------------------------------------------------------------------
    // Team endpoints
    // -----------------------------------------------------------------------

    @PostMapping("/team/league/{leagueId}")
    public ResponseEntity<Void> createTeamInLeague(@RequestBody TeamDetails team,
                                                    @PathVariable String leagueId) {
        rosterService.createTeamInLeague(team, leagueId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/team/{id}")
    public ResponseEntity<TeamDetails> getTeam(@PathVariable String id) {
        TeamDetails team = rosterService.getTeam(id);
        if (team == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(team);
    }

    @DeleteMapping("/team/{id}")
    public ResponseEntity<Void> removeTeam(@PathVariable String id) {
        rosterService.removeTeam(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/team/{id}/players")
    public List<PlayerDetails> getPlayersOfTeam(@PathVariable String id) {
        return rosterService.getPlayersOfTeam(id);
    }

    // -----------------------------------------------------------------------
    // Player endpoints
    // -----------------------------------------------------------------------

    @PostMapping("/player")
    public ResponseEntity<Void> createPlayer(
            @RequestParam String id,
            @RequestParam String name,
            @RequestParam String position,
            @RequestParam double salary) {
        rosterService.createPlayer(id, name, position, salary);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/player/{id}")
    public ResponseEntity<PlayerDetails> getPlayer(@PathVariable String id) {
        try {
            PlayerDetails player = rosterService.getPlayer(id);
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
            rosterService.removePlayer(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/player/{pid}/team/{tid}")
    public ResponseEntity<Void> addPlayerToTeam(@PathVariable String pid, @PathVariable String tid) {
        rosterService.addPlayer(pid, tid);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/player/{pid}/team/{tid}")
    public ResponseEntity<Void> dropPlayerFromTeam(@PathVariable String pid, @PathVariable String tid) {
        rosterService.dropPlayer(pid, tid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/player/{id}/leagues")
    public List<LeagueDetails> getLeaguesOfPlayer(@PathVariable String id) {
        return rosterService.getLeaguesOfPlayer(id);
    }

    @GetMapping("/player/{id}/sports")
    public List<String> getSportsOfPlayer(@PathVariable String id) {
        return rosterService.getSportsOfPlayer(id);
    }

    // -----------------------------------------------------------------------
    // Query endpoints
    // -----------------------------------------------------------------------

    @GetMapping("/players")
    public List<PlayerDetails> getAllPlayers() {
        return rosterService.getAllPlayers();
    }

    @GetMapping("/players/position/{position}")
    public List<PlayerDetails> getPlayersByPosition(@PathVariable String position) {
        return rosterService.getPlayersByPosition(position);
    }

    @GetMapping("/players/salary/higher/{name}")
    public List<PlayerDetails> getPlayersByHigherSalary(@PathVariable String name) {
        return rosterService.getPlayersByHigherSalary(name);
    }

    @GetMapping("/players/salary/range")
    public List<PlayerDetails> getPlayersBySalaryRange(
            @RequestParam double low,
            @RequestParam double high) {
        return rosterService.getPlayersBySalaryRange(low, high);
    }

    @GetMapping("/players/league/{id}")
    public List<PlayerDetails> getPlayersByLeague(@PathVariable String id) {
        return rosterService.getPlayersByLeagueId(id);
    }

    @GetMapping("/players/sport/{sport}")
    public List<PlayerDetails> getPlayersBySport(@PathVariable String sport) {
        return rosterService.getPlayersBySport(sport);
    }

    @GetMapping("/players/city/{city}")
    public List<PlayerDetails> getPlayersByCity(@PathVariable String city) {
        return rosterService.getPlayersByCity(city);
    }

    @GetMapping("/players/not-on-team")
    public List<PlayerDetails> getPlayersNotOnTeam() {
        return rosterService.getPlayersNotOnTeam();
    }
}
