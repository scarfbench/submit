# Migration Changelog: Spring Boot to Jakarta EE

## [2026-03-16T22:00:00Z] [info] Project Analysis
- Identified multi-module Maven project (roster-boot, roster-common) using Spring Boot 3.1.0
- Spring Boot modules: roster-boot (main app), roster-common (shared utilities)
- Entity classes already use `jakarta.persistence` annotations (in `jakartaee.tutorial.roster.entity` package)
- Spring-specific code in `spring.tutorial.roster` package:
  - `RosterApplication.java` - Spring Boot main class with `@SpringBootApplication`
  - `LeagueRepository`, `PlayerRepository`, `TeamRepository` - Spring Data JPA repositories
  - `RequestBean` - `@Service` with `@Transactional` (Spring annotations)
  - `RosterController` - `@RestController` with `@RequestMapping` (Spring MVC)
- Common classes: `Request` interface, `LeagueDetails`, `PlayerDetails`, `TeamDetails`, `IncorrectSportException`
- Dependencies: Spring Boot Starter Web, Spring Boot Starter Data JPA, JoinFaces/PrimeFaces, H2, Jakarta Validation, Jakarta Annotation
- Existing tests: unit test (Mockito), 2 integration tests (`@SpringBootTest`), 1 smoke test
- Dockerfile: maven-based build with Python/Playwright for testing

## [2026-03-16T22:01:00Z] [info] Smoke Test Generation
- Created `smoke.py` with 27 comprehensive HTTP smoke tests covering all REST endpoints
- Tests cover: CRUD operations for League, Team, Player; all query endpoints; error handling (400 for bad sport)
- Tests run sequentially with ordered state dependencies
- Added server readiness wait with 120s timeout

## [2026-03-16T22:02:00Z] [info] Dependency Migration
- Replaced multi-module parent POM with single-module WAR project
- Changed groupId from `spring.tutorial` to `jakartaee.tutorial`
- Changed packaging from `pom` to `war`
- Removed all Spring Boot dependencies:
  - `org.springframework.boot:spring-boot-starter-parent` (parent POM)
  - `org.springframework.boot:spring-boot-starter-web`
  - `org.springframework.boot:spring-boot-starter-data-jpa`
  - `org.springframework.boot:spring-boot-starter-test`
  - `org.joinfaces:joinfaces-bom` and `primefaces-spring-boot-starter`
  - `org.springframework.boot:spring-boot-maven-plugin`
- Added Jakarta EE 10 API: `jakarta.platform:jakarta.jakartaee-api:10.0.0` (provided scope)
- Retained test dependencies: JUnit Jupiter 5.10.2, Mockito 5.11.0
- Added `maven-war-plugin:3.4.0`, updated `maven-compiler-plugin:3.12.1`, `maven-surefire-plugin:3.2.5`
- Removed `roster-common` and `roster-boot` sub-modules (merged into single module)

## [2026-03-16T22:03:00Z] [info] Code Refactoring - Package Migration
- Moved all classes from `spring.tutorial.roster` to `jakartaee.tutorial.roster` package
- Files moved/recreated:
  - `util/IncorrectSportException.java`
  - `util/LeagueDetails.java` (added setters for JSON-B deserialization)
  - `util/PlayerDetails.java` (added setters for JSON-B deserialization)
  - `util/TeamDetails.java` (added setters for JSON-B deserialization)
  - `request/Request.java` (interface - updated imports only)

## [2026-03-16T22:04:00Z] [info] Code Refactoring - Spring Data JPA to JPA EntityManager
- Removed Spring Data JPA repository interfaces (`LeagueRepository`, `PlayerRepository`, `TeamRepository`)
- Rewrote `RequestBean` to use `jakarta.persistence.EntityManager` with JPQL queries
- Replaced Spring Data derived query methods with JPQL TypedQuery equivalents:
  - `findByPosition` -> `SELECT p FROM Player p WHERE p.position = :position`
  - `findBySalaryBetween` -> `SELECT p FROM Player p WHERE p.salary BETWEEN :low AND :high`
  - `findBySalaryGreaterThan` -> `SELECT p FROM Player p WHERE p.salary > :salary`
  - `findByTeams_Id` -> `SELECT p FROM Player p JOIN p.teams t WHERE t.id = :teamId`
  - `findByTeamsIsEmpty` -> `SELECT p FROM Player p WHERE p.teams IS EMPTY`
  - `findByPositionAndName` -> `SELECT p FROM Player p WHERE p.position = :position AND p.name = :name`
  - `findDistinctByTeams_League_Id` -> `SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.id = :leagueId`
  - `findDistinctByTeams_League_Sport` -> `SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.league.sport = :sport`
  - `findDistinctByTeams_City` -> `SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.city = :city`
  - `findFirstByName` -> `SELECT p FROM Player p WHERE p.name = :name`
- Changed `save()`/`deleteById()` to `em.persist()`/`em.merge()`/`em.remove()`
- Added `@PersistenceContext(unitName = "roster-pu")` for EntityManager injection

## [2026-03-16T22:05:00Z] [info] Code Refactoring - Spring DI to CDI
- Replaced `@Service("requestBean")` with `@ApplicationScoped` (CDI)
- Replaced `@org.springframework.transaction.annotation.Transactional` with `@jakarta.transaction.Transactional` (JTA)
- Replaced constructor injection with `@PersistenceContext` field injection for EntityManager
- Retained constructor for unit testing purposes

## [2026-03-16T22:06:00Z] [info] Code Refactoring - Spring MVC to JAX-RS
- Created `RosterApplication extends jakarta.ws.rs.core.Application` with `@ApplicationPath("/")`
- Rewrote `RosterController`:
  - `@RestController` -> `@Path("/roster")` with `@Produces`/`@Consumes` (MediaType.APPLICATION_JSON)
  - `@RequestMapping` -> `@Path`
  - `@GetMapping`/`@PostMapping`/`@DeleteMapping` -> `@GET`/`@POST`/`@DELETE`
  - `@PathVariable` -> `@PathParam`
  - `@RequestParam` -> `@QueryParam`
  - `@RequestBody` -> no annotation (JAX-RS auto-binds entity body)
  - `ResponseEntity` -> `jakarta.ws.rs.core.Response`
  - `@Autowired` -> `@Inject` (CDI)
  - Added `@Consumes(MediaType.WILDCARD)` on endpoints that don't receive JSON body

## [2026-03-16T22:07:00Z] [info] Entity Classes
- Entity classes (`League`, `Player`, `Team`, `SummerLeague`, `WinterLeague`) already used Jakarta Persistence annotations
- Moved to `src/main/java/jakartaee/tutorial/roster/entity/` (flat project structure)
- Updated `SummerLeague` and `WinterLeague` import from `spring.tutorial.roster.util.IncorrectSportException` to `jakartaee.tutorial.roster.util.IncorrectSportException`

## [2026-03-16T22:08:00Z] [info] Configuration Files
- Created `src/main/resources/META-INF/persistence.xml`:
  - Persistence unit: `roster-pu`, transaction-type: JTA
  - JTA data source: `jdbc/rosterDS`
  - Listed all entity classes explicitly
  - Schema generation: `create` (auto-create tables)
- Created `src/main/webapp/WEB-INF/beans.xml`:
  - CDI beans discovery mode: `all`
  - Jakarta CDI 4.0 schema
- Removed `application.properties` (Spring-specific configuration)

## [2026-03-16T22:09:00Z] [info] Open Liberty Server Configuration
- Created `src/main/liberty/config/server.xml`:
  - Features: restfulWS-3.1, jsonb-3.0, cdi-4.0, persistence-3.1, jdbc-4.2, servlet-6.0
  - HTTP endpoint on port 8080, HTTPS disabled (httpsPort=-1)
  - H2 datasource configured via JNDI (jdbc/rosterDS)
  - WAR deployed with context root "/"
  - H2 library shared via classloader

## [2026-03-16T22:10:00Z] [info] Dockerfile Update
- Updated Dockerfile for Open Liberty deployment:
  - Base image: `maven:3.9.12-ibm-semeru-21-noble` (retained for build tooling)
  - Added `unzip` package installation
  - Install Open Liberty 24.0.0.1 via Maven dependency:copy plugin
  - Build WAR with `mvn clean package -DskipTests`
  - Create Liberty server, deploy WAR, copy server.xml
  - Download H2 JDBC driver from Maven Central into Liberty server lib
  - CMD changed from `mvn spring-boot:run` to `server run defaultServer`

## [2026-03-16T22:10:30Z] [warning] Open Liberty Download Issue
- Initial attempt to download Open Liberty from IBM's public CDN failed (connection reset)
- Resolution: Used Maven dependency:copy plugin to download from Maven Central instead

## [2026-03-16T22:11:00Z] [warning] Datasource Configuration Issue
- First attempt used `properties.generic` element which caused URL format error with H2
- Resolution: Changed to `properties` element in server.xml datasource configuration

## [2026-03-16T22:11:30Z] [warning] SSL/Transport Security Issue
- Full Jakarta EE 10 feature set (`jakartaee-10.0`) enabled SSL/transport security by default
- Caused HTTP connection reset on port 8080
- Resolution: Switched to minimal feature set (restfulWS-3.1, jsonb-3.0, cdi-4.0, persistence-3.1, jdbc-4.2, servlet-6.0) and disabled HTTPS (httpsPort=-1)

## [2026-03-16T22:12:00Z] [info] Test Updates
- Rewrote `RequestBeanUnitTest` for Jakarta EE:
  - Replaced Spring Data repository mocks with `EntityManager` mock
  - Verified `em.persist()` calls instead of `repository.save()`
  - Verified `em.merge()` calls for team updates
  - Both test methods (createLeagueChoosesSeasonType, addPlayerLinksAndSavesTeam) adapted
- Removed Spring Boot integration tests (require Spring test context):
  - `RequestCrudIT.java` - replaced by smoke tests
  - `RequestQueriesIT.java` - replaced by smoke tests
  - `RequestSmokeText,java` (malformed filename) - replaced by smoke tests

## [2026-03-16T22:13:00Z] [info] Build & Compilation Success
- Maven build completed successfully: `mvn clean package -DskipTests` -> BUILD SUCCESS
- WAR file produced: `target/roster.war`
- Docker image built successfully

## [2026-03-16T22:14:00Z] [info] Runtime Verification
- Open Liberty server started successfully in ~12 seconds
- Features loaded: cdi-4.0, jdbc-4.2, jndi-1.0, jsonb-3.0, jsonp-2.1, persistence-3.1, persistenceContainer-3.1, restfulWS-3.1, restfulWSClient-3.1, servlet-6.0
- Application deployed at context root "/"
- H2 in-memory database initialized via JPA schema generation

## [2026-03-16T22:15:00Z] [info] Smoke Test Results
- All 27 smoke tests PASSED:
  - League CRUD: create, get, delete
  - Team CRUD: create in league, get, delete, get teams of league
  - Player CRUD: create, get, delete, add to team, drop from team
  - Query endpoints: by position, salary range, higher salary, league, sport, city, not on team, position+name
  - League/Sport relationships: get leagues of player, get sports of player
  - Winter league creation and retrieval
  - Error handling: bad sport returns HTTP 400

## [2026-03-16T22:15:30Z] [info] Migration Complete
- All business logic and REST API behavior preserved
- All 27 smoke tests pass
- Application builds, deploys, and runs on Open Liberty with Jakarta EE 10
