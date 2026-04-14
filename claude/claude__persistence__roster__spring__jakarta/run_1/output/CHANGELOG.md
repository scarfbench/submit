# Migration Changelog: Spring Boot -> Jakarta EE

## [2026-03-16T21:45:00Z] [info] Project Analysis
- Identified multi-module Maven project: roster (parent), roster-common, roster-boot
- Spring Boot 3.1.0 application with Spring Data JPA, Spring MVC REST, H2 in-memory DB
- 5 JPA entities (League, SummerLeague, WinterLeague, Team, Player) already using jakarta.persistence
- 3 Spring Data JPA repositories (LeagueRepository, PlayerRepository, TeamRepository)
- 1 Spring REST controller (RosterController) with 20+ endpoints
- 1 Spring @Service (RequestBean) implementing Request interface
- 3 test files (unit test, 2 integration tests), 1 misnamed file (comma in filename)
- Packages: spring.tutorial.roster (Spring code), jakartaee.tutorial.roster.entity (entities)

## [2026-03-16T21:48:00Z] [info] Smoke Test Generation
- Created smoke.py with 5 test suites covering all REST endpoints
- Tests: League CRUD, Team CRUD, Player CRUD, Query Endpoints, Drop/Remove Operations
- Uses Python requests library with configurable BASE_URL
- Includes server readiness check with 120-second timeout

## [2026-03-16T21:50:00Z] [info] Dependency Migration - Parent POM
- Changed groupId from spring.tutorial to jakartaee.tutorial
- Removed Spring Boot parent reference
- Added explicit compiler settings for Java 17

## [2026-03-16T21:50:30Z] [info] Dependency Migration - roster-common POM
- Updated parent groupId to jakartaee.tutorial
- Retained jakarta.jakartaee-api 10.0.0 dependency (already correct)

## [2026-03-16T21:51:00Z] [info] Dependency Migration - roster-boot POM
- Removed Spring Boot parent (spring-boot-starter-parent 3.1.0)
- Updated parent to jakartaee.tutorial:roster:1.0.0
- Changed packaging from jar to war (for Jakarta EE app server deployment)
- Removed Spring dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-test
- Removed joinfaces/primefaces dependencies
- Added jakarta.jakartaee-api 10.0.0 (provided scope)
- Added explicit H2 2.2.224 (runtime scope)
- Added JUnit Jupiter 5.10.2, Mockito 5.11.0 for tests
- Added maven-war-plugin 3.4.0
- Added liberty-maven-plugin 3.10 for Open Liberty support
- Set finalName to roster-boot

## [2026-03-16T21:53:00Z] [info] Package Restructuring
- Moved all classes from spring.tutorial.roster to jakartaee.tutorial.roster
- roster-common: Moved Request, IncorrectSportException, LeagueDetails, PlayerDetails, TeamDetails
- roster-boot: Moved RequestBean, created new RosterResource (JAX-RS), RosterApplication (JAX-RS)
- Updated all import statements from spring.tutorial to jakartaee.tutorial
- Added setters to Details classes for JSON-B deserialization

## [2026-03-16T21:55:00Z] [info] Spring Data JPA -> Jakarta Persistence Migration
- Removed Spring Data JPA repositories (LeagueRepository, PlayerRepository, TeamRepository)
- Replaced with EntityManager-based JPQL queries in RequestBean
- @PersistenceContext(unitName = "roster-pu") for EntityManager injection
- Converted all Spring Data derived query methods to explicit JPQL queries:
  - findByPosition -> SELECT p FROM Player p WHERE p.position = :position
  - findBySalaryBetween -> SELECT p FROM Player p WHERE p.salary BETWEEN :low AND :high
  - findBySalaryGreaterThan -> SELECT p FROM Player p WHERE p.salary > :salary
  - findByTeams_Id -> SELECT p FROM Player p JOIN p.teams t WHERE t.id = :teamId
  - findByTeamsIsEmpty -> SELECT p FROM Player p WHERE p.teams IS EMPTY
  - findByPositionAndName -> SELECT p FROM Player p WHERE p.position = :position AND p.name = :name
  - findDistinctByTeams_League_Id -> SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.id = :leagueId
  - findDistinctByTeams_League_Sport -> SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.sport = :sport
  - findDistinctByTeams_City -> SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.city = :city
  - findByLeague_Id -> SELECT t FROM Team t WHERE t.league.id = :leagueId

## [2026-03-16T21:57:00Z] [info] Spring REST Controller -> JAX-RS Resource Migration
- Replaced @RestController with JAX-RS @Path("/roster")
- Replaced @RequestMapping with @Path, @GET, @POST, @DELETE
- Replaced @PathVariable with @PathParam
- Replaced @RequestParam with @QueryParam
- Replaced @RequestBody with unmarshalled JSON-B parameters
- Replaced ResponseEntity with jakarta.ws.rs.core.Response
- Added @Produces(APPLICATION_JSON) and @Consumes(APPLICATION_JSON)

## [2026-03-16T21:58:00Z] [info] Spring Service -> CDI Bean Migration
- Replaced @Service("requestBean") with @ApplicationScoped
- Replaced Spring @Transactional with jakarta.transaction.Transactional
- Replaced constructor injection of repositories with @PersistenceContext EntityManager
- Removed @PostConstruct init() method (empty)
- Changed save() calls to em.persist() / em.merge()
- Changed deleteById() calls to em.find() + em.remove()

## [2026-03-16T21:59:00Z] [info] JAX-RS Application Class
- Created RosterApplication extends jakarta.ws.rs.core.Application
- @ApplicationPath("/") for root context path
- Replaced Spring Boot main class (SpringApplication.run)

## [2026-03-16T22:00:00Z] [info] Jakarta EE Configuration Files Created
- server.xml: Open Liberty configuration with jakartaee-10.0 feature, H2 data source, HTTP endpoint on 8080
- persistence.xml: JTA persistence unit "roster-pu" with jdbc/roster JNDI data source, drop-and-create schema generation
- beans.xml: CDI 4.0 with bean-discovery-mode="all"

## [2026-03-16T22:01:00Z] [info] Entity Import Updates
- SummerLeague.java: Changed import from spring.tutorial.roster.util.IncorrectSportException to jakartaee.tutorial.roster.util.IncorrectSportException
- WinterLeague.java: Same import change

## [2026-03-16T22:02:00Z] [info] Dockerfile Migration
- Changed from single-stage Spring Boot build to multi-stage build
- Stage 1 (build): maven:3.9.12-eclipse-temurin-17 for compilation
- Stage 2 (runtime): icr.io/appcafe/open-liberty:full-java17-openj9-ubi
- Installed Python 3.9 via dnf for smoke tests
- Copies server.xml, H2 jar, WAR, and smoke.py to Liberty server
- CMD changed from mvn spring-boot:run to Liberty server run

## [2026-03-16T22:03:00Z] [info] Test Updates
- Updated RequestBeanUnitTest to use reflection-based EntityManager injection (no Spring context)
- Moved test to jakartaee.tutorial.roster package
- Removed Spring integration tests (RequestCrudIT, RequestQueriesIT, RequestSmokeTest) - replaced by smoke.py
- Fixed misnamed test file (RequestSmokeText,java with comma)

## [2026-03-16T22:04:00Z] [info] Old Files Removed
- Deleted all files under roster-boot/src/main/java/spring/
- Deleted all files under roster-boot/src/test/java/spring/
- Deleted all files under roster-common/src/main/java/spring/
- Deleted roster-boot/src/main/resources/application.properties (Spring-specific)

## [2026-03-16T22:05:00Z] [warning] Dockerfile - apt-get Not Available
- File: Dockerfile
- Issue: Open Liberty UBI image uses RHEL, not Debian - apt-get not found
- Resolution: Changed to dnf for package installation

## [2026-03-16T22:06:00Z] [warning] Dockerfile - microdnf Not Available
- File: Dockerfile
- Issue: UBI image has dnf, not microdnf
- Resolution: Changed from microdnf to dnf

## [2026-03-16T22:07:00Z] [warning] H2 Data Source URL Error
- File: server.xml
- Issue: properties.generic element did not pass URL to H2 JDBC driver correctly
- Resolution: Changed to properties element with URL attribute

## [2026-03-16T22:08:00Z] [info] Docker Build Cached Stale Artifacts
- Issue: Cached build layer contained old Spring code alongside new Jakarta code
- Resolution: Rebuilt with --no-cache flag

## [2026-03-16T22:09:00Z] [info] Docker Build Success
- Maven build completed successfully: roster (pom), roster-common (jar), roster-boot (war)
- Build time: ~25 seconds
- Docker image created with Open Liberty runtime

## [2026-03-16T22:10:00Z] [info] Application Startup Verified
- Open Liberty started in ~10 seconds
- Jakarta EE 10 features loaded: jakartaee-10.0, cdi-4.0, persistence-3.1, restfulWS-3.1, etc.
- Web application available at http://localhost:8080/
- DROP TABLE warnings during schema creation (expected for fresh in-memory DB)
- ORB/user-registry warning (safely ignored - not using EJB remoting)

## [2026-03-16T22:11:00Z] [info] Smoke Tests - All Passed
- 5/5 test suites passed, 0 failures
- League CRUD: Create, retrieve, delete leagues (summer + winter); 404 for non-existent
- Team CRUD: Create teams in leagues, retrieve, list teams of league
- Player CRUD: Create players, add to teams, retrieve, list players of team
- Query Endpoints: All 11 query endpoints verified (position, salary, league, sport, city, not-on-team, position+name, leagues-of-player, sports-of-player)
- Drop/Remove: Drop player from team, remove player, remove team, remove league - all verified

## [2026-03-16T22:12:00Z] [info] Migration Complete
- Framework: Spring Boot 3.1.0 -> Jakarta EE 10.0 (Open Liberty)
- All 20+ REST endpoints functional
- All business logic preserved
- In-memory H2 database working
- Docker image builds and runs successfully
