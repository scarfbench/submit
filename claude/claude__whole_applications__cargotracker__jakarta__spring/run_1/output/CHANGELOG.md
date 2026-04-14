# Migration Report: Jakarta EE to Spring Boot

## Summary

Successfully migrated the Eclipse Cargo Tracker application from Jakarta EE 8 (deployed on Payara/OpenLiberty) to Spring Boot 2.7.18. The application preserves its Domain-Driven Design (DDD) architecture while replacing all Jakarta EE APIs with Spring equivalents.

**Source framework:** Jakarta EE 8 (CDI, EJB, JPA, JMS, JSF/PrimeFaces, JAX-RS, Jakarta Batch)
**Target framework:** Spring Boot 2.7.18 (Spring MVC, Spring Data JPA, Spring Events, Spring Scheduling)
**Java version:** 8 -> 11
**Packaging:** WAR -> executable JAR

## Smoke Test Results

**8/8 tests passed:**
- List all cargo (4 cargo items found)
- List shipping locations (13 locations found)
- Get cargo details by tracking ID (ABC123)
- Track cargo status (ABC123)
- List tracking IDs (4 IDs including ABC123)
- Graph traversal / shortest path (transit paths found)
- Public tracking API (ABC123)
- Submit handling report (UNLOAD event)

## Changes by Category

### Project Configuration

#### pom.xml (rewritten)
- Changed parent to `spring-boot-starter-parent:2.7.18`
- Changed packaging from WAR to JAR
- Replaced Jakarta EE BOM with Spring Boot starters:
  - `spring-boot-starter-web` (embedded Tomcat, Spring MVC)
  - `spring-boot-starter-data-jpa` (Hibernate, JPA)
  - `spring-boot-starter-validation` (Bean Validation)
  - `h2` database (runtime)
  - `commons-lang3`
  - `jackson-datatype-jsr310` (Java 8 date/time serialization)
  - `spring-boot-starter-test` (test scope)
- Added `spring-boot-maven-plugin` for executable JAR
- Final artifact name: `cargo-tracker`

#### src/main/resources/application.properties (new)
- H2 in-memory database configuration (`jdbc:h2:mem:cargotracker`)
- JPA auto-DDL with `create` strategy
- Deferred datasource initialization for data.sql
- Context path: `/cargo-tracker`
- Graph traversal URL property: `app.graphTraversalUrl`

#### src/main/resources/data.sql (new)
- Initial `application_settings` row for sample data loading

#### Dockerfile (rewritten)
- Multi-stage build: `maven:3.9.9-eclipse-temurin-11` (build) + `eclipse-temurin:11-jre` (runtime)
- Installs Python 3, pip, curl, and uv for smoke tests
- Copies built JAR and smoke tests
- CMD: `java -jar app.jar`

### Application Entry Point

#### CargoTrackerApplication.java (new)
- `@SpringBootApplication` with `scanBasePackages` for both `org.eclipse.cargotracker` and `org.eclipse.pathfinder`
- `@EnableAsync` for asynchronous event processing
- `@EnableScheduling` for scheduled tasks (file upload scanning)

### Removed Files

- `src/test/` - Arquillian-based integration tests (not compatible with Spring Boot)
- `src/main/webapp/` - All JSF/PrimeFaces XHTML views (replaced by REST controllers)
- `src/main/liberty/` - OpenLiberty server configuration
- `src/main/resources/META-INF/persistence.xml` - JPA persistence unit (managed by Spring Boot auto-config)
- `src/main/resources/META-INF/batch-jobs/` - Jakarta Batch job XMLs

### Domain Model (minimal changes)

Files preserved with no or minimal changes (javax.persistence.* annotations remain valid):

- `domain/model/cargo/Cargo.java` - Root aggregate entity
- `domain/model/cargo/CargoRepository.java` - Repository interface
- `domain/model/cargo/Delivery.java` - Value object
- `domain/model/cargo/Itinerary.java` - Value object
- `domain/model/cargo/Leg.java` - Entity
- `domain/model/cargo/RouteSpecification.java` - Value object
- `domain/model/cargo/TrackingId.java` - Value object
- `domain/model/cargo/TransportStatus.java` - Enum
- `domain/model/cargo/RoutingStatus.java` - Enum
- `domain/model/handling/HandlingEvent.java` - Entity
- `domain/model/handling/HandlingEventRepository.java` - Repository interface
- `domain/model/handling/HandlingHistory.java` - Value object
- `domain/model/handling/CannotCreateHandlingEventException.java` - Removed `@ApplicationException`
- `domain/model/handling/HandlingEventFactory.java` - `@ApplicationScoped` -> `@Component`, `@Inject` -> `@Autowired`
- `domain/model/handling/UnknownCargoException.java` - No change
- `domain/model/handling/UnknownVoyageException.java` - No change
- `domain/model/handling/UnknownLocationException.java` - No change
- `domain/model/location/Location.java` - Entity
- `domain/model/location/LocationRepository.java` - Repository interface
- `domain/model/location/SampleLocations.java` - Constants
- `domain/model/location/UnLocode.java` - Value object
- `domain/model/voyage/Voyage.java` - Entity
- `domain/model/voyage/VoyageRepository.java` - Repository interface
- `domain/model/voyage/VoyageNumber.java` - Value object
- `domain/model/voyage/CarrierMovement.java` - Entity
- `domain/model/voyage/Schedule.java` - Value object
- `domain/model/voyage/SampleVoyages.java` - Constants
- All specification classes in `domain/shared/` - No change

### Application Services

#### DefaultBookingService.java
- `@Stateless` -> `@Service` + `@Transactional`
- `@Inject` -> `@Autowired`
- Logger: `@Inject Logger` -> `private static final Logger logger = Logger.getLogger(...)`

#### DefaultCargoInspectionService.java
- `@Stateless` -> `@Service` + `@Transactional`
- `@Inject` -> `@Autowired`
- Logger: static final

#### DefaultHandlingEventService.java
- `@Stateless` -> `@Service` + `@Transactional`
- `@Inject` -> `@Autowired`
- Logger: static final

#### ApplicationEvents.java (interface)
- No change (already POJO interface)

#### HandlingEventRegistrationAttempt.java
- No change (already a POJO)

### Infrastructure Layer

#### JPA Repositories

All repositories: `@ApplicationScoped` -> `@Repository`, removed `Serializable`

- `JpaCargoRepository.java` - CDI `Event<Cargo>` -> `ApplicationEventPublisher`
- `JpaVoyageRepository.java`
- `JpaLocationRepository.java`
- `JpaHandlingEventRepository.java`

#### JmsApplicationEvents.java (complete rewrite)
- JMS queue sending -> Spring `ApplicationEventPublisher`
- Defined inner event classes:
  - `CargoHandledEvent`
  - `CargoMisdirectedEvent`
  - `CargoDeliveredEvent`
  - `HandlingEventRegistrationAttemptEvent`
  - `RejectedRegistrationAttemptEvent`

#### Message Consumers (all rewritten from @MessageDriven to Spring events)

- `CargoHandledConsumer.java` - `@Component` + `@EventListener` + `@Async`, listens for `CargoHandledEvent`
- `MisdirectedCargoConsumer.java` - Logs misdirected cargo events
- `DeliveredCargoConsumer.java` - Logs delivered cargo events
- `HandlingEventRegistrationAttemptConsumer.java` - Processes registration attempts via `HandlingEventService`
- `RejectedRegistrationAttemptsConsumer.java` - Logs rejected registration attempts

#### ExternalRoutingService.java (significant rewrite)
- `@Stateless` -> `@Service`
- JAX-RS `Client`/`WebTarget` -> Spring `RestTemplate`
- `@Resource(lookup="java:app/configuration/GraphTraversalUrl")` -> `@Value("${app.graphTraversalUrl}")`

#### SampleDataGenerator.java (significant rewrite)
- `@Singleton` `@Startup` -> `@Component implements CommandLineRunner`
- `@PostConstruct` -> `run(String... args)` with `@Transactional`
- `@PersistenceContext EntityManager` -> `@Autowired EntityManager`
- Logger: static final

#### UploadDirectoryScanner.java
- `@Singleton` with `@Schedule` -> `@Component` with `@Scheduled(fixedRate = 300000)`

### Interface Layer

#### REST Controllers (new, replacing JSF backing beans)

##### CargoAdminController.java (new - replaces 6 JSF beans)
Consolidates: CargoDetails, ChangeArrivalDeadline, ChangeDestination, Booking, CargoAdmin, RouteAssignment
- `GET /api/cargo/list` - List all cargo
- `GET /api/cargo/{trackingId}` - Get cargo details
- `GET /api/cargo/{trackingId}/track` - Track cargo status
- `POST /api/cargo/book` - Book new cargo
- `GET /api/cargo/locations` - List available locations
- `GET /api/cargo/{trackingId}/routes` - Get route candidates
- `POST /api/cargo/{trackingId}/assignRoute` - Assign route
- `POST /api/cargo/{trackingId}/changeDestination` - Change destination
- `POST /api/cargo/{trackingId}/changeDeadline` - Change arrival deadline
- `GET /api/cargo/trackingIds` - List all tracking IDs

##### HandlingEventController.java (new - replaces EventLogger JSF bean)
- `POST /api/handling/register` - Register handling event

##### Track.java (rewritten from JSF to REST)
- `@Named` `@ViewScoped` -> `@RestController` `@RequestMapping("/api/track")`
- `GET /api/track?trackingId=...` - Public cargo tracking

#### REST Services (rewritten from JAX-RS to Spring MVC)

##### HandlingReportService.java
- `@Path("/handling")` `@Stateless` -> `@RestController` `@RequestMapping("/rest/handling")`
- `@POST` `@Consumes(MediaType.APPLICATION_JSON)` -> `@PostMapping("/reports")` `@RequestBody`

##### HandlingReport.java
- Removed `@XmlRootElement`, kept `javax.validation` annotations

#### Pathfinder Service

##### GraphTraversalService.java (rewritten)
- `@Path("/graph-traversal")` -> `@RestController` `@RequestMapping("/rest/graph-traversal")`
- `@GET` `@QueryParam` -> `@GetMapping` `@RequestParam`
- Returns `List<TransitPath>` directly (Jackson serialization)

##### GraphDao.java
- `@ApplicationScoped` -> `@Component`

##### TransitPath.java, TransitEdge.java
- Removed `@XmlRootElement` (not needed with Jackson)

#### Configuration

##### RestConfiguration.java
- JAX-RS `Application` subclass -> empty `@Configuration` class

### Errors Encountered and Resolved

1. **Logger autowiring** - Spring cannot auto-wire `java.util.logging.Logger`. Fixed by replacing `@Inject Logger` / `@Autowired Logger` with `private static final Logger logger = Logger.getLogger(ClassName.class.getName())` in all service classes.

2. **Missed JSF/JAX-RS imports** - `Track.java`, `HandlingReportService.java`, and `HandlingReport.java` retained Jakarta EE imports after initial migration pass. Fixed by fully rewriting each file.

3. **Missing RejectedRegistrationAttemptEvent** - `RejectedRegistrationAttemptsConsumer` referenced `JmsApplicationEvents.RejectedRegistrationAttemptEvent` which didn't exist. Fixed by adding the inner class to `JmsApplicationEvents`.

4. **Docker uv path** - The `uv` package manager installed to `$HOME/.local/bin/` instead of `$HOME/.cargo/bin/`. Fixed with fallback symlink logic in Dockerfile.

### API Mapping Summary

| Original (Jakarta EE) | New (Spring Boot) |
|---|---|
| JSF pages at `/cargo/*` | REST API at `/api/cargo/*` |
| JSF page at `/event-logger.xhtml` | REST API at `/api/handling/register` |
| JSF page at `/track.xhtml` | REST API at `/api/track?trackingId=` |
| JAX-RS at `/rest/handling/reports` | Spring MVC at `/rest/handling/reports` (same path) |
| JAX-RS at `/rest/graph-traversal/shortest-path` | Spring MVC at `/rest/graph-traversal/shortest-path` (same path) |
| JMS queues for async events | Spring ApplicationEvents with @Async |
| JNDI lookups | `@Value` property injection |
| EJB timers | `@Scheduled` methods |
