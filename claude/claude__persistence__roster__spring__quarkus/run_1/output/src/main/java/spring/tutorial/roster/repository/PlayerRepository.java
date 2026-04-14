package spring.tutorial.roster.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakartaee.tutorial.roster.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, String> {

  @Query("SELECT p FROM Player p WHERE p.position = :position")
  List<Player> findByPosition(@Param("position") String position);

  @Query("SELECT p FROM Player p WHERE p.salary BETWEEN :low AND :high")
  List<Player> findBySalaryBetween(@Param("low") double low, @Param("high") double high);

  @Query("SELECT p FROM Player p WHERE p.salary > :salary")
  List<Player> findBySalaryGreaterThan(@Param("salary") double salary);

  @Query("SELECT p FROM Player p JOIN p.teams t WHERE t.id = :teamId")
  List<Player> findByTeams_Id(@Param("teamId") String teamId);

  @Query("SELECT p FROM Player p WHERE p.teams IS EMPTY")
  List<Player> findByTeamsIsEmpty();

  @Query("SELECT p FROM Player p WHERE p.position = :position AND p.name = :name")
  List<Player> findByPositionAndName(@Param("position") String position, @Param("name") String name);

  @Query("SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.id = :leagueId")
  List<Player> findDistinctByTeams_League_Id(@Param("leagueId") String leagueId);

  @Query("SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.sport = :sport")
  List<Player> findDistinctByTeams_League_Sport(@Param("sport") String sport);

  @Query("SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.city = :city")
  List<Player> findDistinctByTeams_City(@Param("city") String city);

  @Query("SELECT p FROM Player p WHERE p.name = :name")
  Optional<Player> findFirstByName(@Param("name") String name);
}
