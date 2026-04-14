package jakartaee.tutorial.roster;

import java.lang.reflect.Field;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakartaee.tutorial.roster.request.RequestBean;
import jakartaee.tutorial.roster.util.LeagueDetails;
import jakartaee.tutorial.roster.entity.League;
import jakartaee.tutorial.roster.entity.Player;
import jakartaee.tutorial.roster.entity.SummerLeague;
import jakartaee.tutorial.roster.entity.Team;
import jakartaee.tutorial.roster.entity.WinterLeague;

@ExtendWith(MockitoExtension.class)
class RequestBeanUnitTest {

    @Mock
    EntityManager em;

    RequestBean bean;

    @BeforeEach
    void setUp() throws Exception {
        bean = new RequestBean();
        // Inject mock EntityManager via reflection
        Field emField = RequestBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(bean, em);
    }

    @Test
    void createLeagueChoosesSeasonType() {
        bean.createLeague(new LeagueDetails("L1", "Name", "soccer"));
        var cap1 = org.mockito.ArgumentCaptor.forClass(League.class);
        verify(em).persist(cap1.capture());
        assertTrue(cap1.getValue() instanceof SummerLeague);

        reset(em);
        bean.createLeague(new LeagueDetails("L2", "Name", "hockey"));
        var cap2 = org.mockito.ArgumentCaptor.forClass(League.class);
        verify(em).persist(cap2.capture());
        assertTrue(cap2.getValue() instanceof WinterLeague);
    }

    @Test
    void addPlayerLinksAndSavesTeam() {
        Player p = new Player("P1", "Alice", "Forward", 100);
        Team t = new Team("T1", "Tigers", "Austin");
        when(em.find(Player.class, "P1")).thenReturn(p);
        when(em.find(Team.class, "T1")).thenReturn(t);

        bean.addPlayer("P1", "T1");

        verify(em).merge(t);
        assertTrue(t.getPlayers().contains(p));
        assertTrue(p.getTeams().contains(t));
    }
}
