package jakartaee.tutorial.roster;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;

import jakartaee.tutorial.roster.request.RequestBean;
import jakartaee.tutorial.roster.util.LeagueDetails;
import jakartaee.tutorial.roster.entity.League;
import jakartaee.tutorial.roster.entity.Player;
import jakartaee.tutorial.roster.entity.SummerLeague;
import jakartaee.tutorial.roster.entity.Team;
import jakartaee.tutorial.roster.entity.WinterLeague;

@ExtendWith(MockitoExtension.class)
class RequestBeanUnitTest {

  @Mock EntityManager em;

  @Test
  void createLeagueChoosesSeasonType() {
    RequestBean bean = new RequestBean(em);

    bean.createLeague(new LeagueDetails("L1","Name","soccer"));
    var cap1 = org.mockito.ArgumentCaptor.forClass(League.class);
    verify(em).persist(cap1.capture());
    assertTrue(cap1.getValue() instanceof SummerLeague);

    reset(em);
    bean.createLeague(new LeagueDetails("L2","Name","hockey"));
    var cap2 = org.mockito.ArgumentCaptor.forClass(League.class);
    verify(em).persist(cap2.capture());
    assertTrue(cap2.getValue() instanceof WinterLeague);
  }

  @Test
  void addPlayerLinksAndSavesTeam() {
    RequestBean bean = new RequestBean(em);
    Player p = new Player("P1","Alice","Forward",100);
    Team t = new Team("T1","Tigers","Austin");
    when(em.find(Player.class, "P1")).thenReturn(p);
    when(em.find(Team.class, "T1")).thenReturn(t);

    bean.addPlayer("P1","T1");

    verify(em).merge(t);
    assertTrue(t.getPlayers().contains(p));
    assertTrue(p.getTeams().contains(t));
  }
}
