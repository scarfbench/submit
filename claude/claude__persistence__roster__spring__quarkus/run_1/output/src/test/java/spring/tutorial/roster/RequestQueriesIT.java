package spring.tutorial.roster;

import java.util.List;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import spring.tutorial.roster.request.Request;
import spring.tutorial.roster.util.LeagueDetails;
import spring.tutorial.roster.util.PlayerDetails;
import spring.tutorial.roster.util.TeamDetails;
import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Transactional
class RequestQueriesIT {

  @Inject
  Request request;

  @Test
  void queryScenarios() {
    request.createLeague(new LeagueDetails("Lq","Summer","soccer"));
    request.createTeamInLeague(new TeamDetails("Tq","Tigers","Austin"), "Lq");
    request.createPlayer("Pa","Alice","Forward",100);
    request.createPlayer("Pb","Bob","Goalie",120);
    request.createPlayer("Pz","Zed","Forward",90);
    request.addPlayer("Pa","Tq");
    request.addPlayer("Pb","Tq");

    assertTrue(ids(request.getPlayersByPosition("Forward")).containsAll(List.of("Pa","Pz")));
    assertEquals(List.of("Pb"), ids(request.getPlayersByHigherSalary("Alice")));
    assertEquals(List.of("Pa"), ids(request.getPlayersBySalaryRange(95,110)));
    assertTrue(ids(request.getPlayersByLeagueId("Lq")).containsAll(List.of("Pa","Pb")));
    assertTrue(ids(request.getPlayersBySport("soccer")).containsAll(List.of("Pa","Pb")));
    assertTrue(ids(request.getPlayersByCity("Austin")).containsAll(List.of("Pa","Pb")));
    assertTrue(ids(request.getPlayersNotOnTeam()).contains("Pz"));
    assertEquals(List.of("Pa"), ids(request.getPlayersByPositionAndName("Forward","Alice")));
    assertEquals(List.of("Lq"), request.getLeaguesOfPlayer("Pa").stream().map(LeagueDetails::getId).toList());
    assertTrue(request.getSportsOfPlayer("Pa").contains("soccer"));
  }

  private static List<String> ids(List<PlayerDetails> list) {
    return list.stream().map(PlayerDetails::getId).toList();
  }
}
