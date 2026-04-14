# CHANGELOG - Quarkus to Spring Boot Migration

## Overview

Migrated the Eclipse Cargo Tracker DDD application from Quarkus to Spring Boot 3.4.3 (Java 21). The application follows Domain-Driven Design with aggregates, repositories, services, and value objects.

## Build & Configuration

### pom.xml
- Replaced Quarkus BOM and plugins with Spring Boot 3.4.3 parent (`spring-boot-starter-parent`)
- Replaced Quarkus dependencies with Spring Boot starters:
  - `spring-boot-starter-web` (replaces quarkus-resteasy, quarkus-jsonb)
  - `spring-boot-starter-data-jpa` (replaces quarkus-hibernate-orm, quarkus-jdbc-h2)
  - `spring-boot-starter-artemis` (replaces quarkus-artemis-jms)
  - `spring-boot-starter-batch` (replaces quarkus-jberet for Jakarta Batch)
  - `spring-boot-starter-test` (replaces quarkus-junit5)
- Added `jackson-databind` for JSON serialization (replaces JSON-B)
- Added `h2` runtime dependency
- Added `spring-boot-maven-plugin` for building executable JARs
- Retained `jakarta.json` API dependency for existing JSON processing code

### application.properties
- Replaced all `quarkus.*` properties with Spring Boot equivalents
- `quarkus.datasource.*` -> `spring.datasource.*`
- `quarkus.hibernate-orm.*` -> `spring.jpa.*`
- Configured ActiveMQ Artemis via `spring.artemis.*` properties
- Set H2 in-memory database: `jdbc:h2:mem:cargo-tracker-database;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- Set `spring.sql.init.mode=never` (data loaded programmatically instead of via import.sql)
- Added `server.servlet.context-path=/cargo-tracker`

### Main Application Class
- Created `CargoTrackerApplication.java` with `@SpringBootApplication` and `@EnableScheduling`

### Dockerfile
- Changed CMD from `mvn quarkus:run` to `java -jar target/cargo-tracker-1.0.0-SNAPSHOT.jar`

## Domain Layer (Unchanged)

The domain model was preserved as-is. No changes to domain entities, value objects, or domain events since they use standard JPA annotations and have no framework-specific dependencies.

## Infrastructure Layer

### Repositories
- Replaced all Quarkus Panache-based repository implementations with standard JPA `EntityManager` implementations
- `@ApplicationScoped` -> `@Repository`
- `@Inject EntityManager` -> `@PersistenceContext EntityManager`
- Replaced Panache query methods (e.g., `find()`, `listAll()`) with JPQL queries via `EntityManager`
- Files changed:
  - `JpaCargoRepository.java`
  - `JpaHandlingEventRepository.java`
  - `JpaLocationRepository.java`
  - `JpaVoyageRepository.java`

### Messaging (JMS)
- Created `JmsConfig.java` with `@Configuration` to define JMS queue beans
- Replaced manual `JMSContext` consumer polling with `@JmsListener` annotations
- `@ApplicationScoped` -> `@Component` / `@Service`
- `@Inject` -> `@Autowired`
- Files changed:
  - `JmsConfig.java` (NEW)
  - `CargoHandledConsumer.java`
  - `DeliveryUpdatedConsumer.java`
  - `HandlingEventRegistrationAttemptConsumer.java`
  - `MisdirectedCargoConsumer.java`
  - `RejectedRegistrationAttemptsConsumer.java`

### External Routing Service
- `ExternalRoutingService.java`: Replaced JAX-RS `Client` with Spring `RestTemplate`
- Replaced `@ConfigProperty` with `@Value` for configuration injection

### Logging
- `LoggingEventHandler.java`: Replaced CDI `@Observes` with Spring `@EventListener`

## Application Layer

### Services
- Replaced CDI annotations with Spring annotations across all service classes
- `@ApplicationScoped` -> `@Service`
- `@Inject` -> `@Autowired`
- `jakarta.transaction.Transactional` -> `org.springframework.transaction.annotation.Transactional`
- CDI `Event.fire()` -> Spring `ApplicationEventPublisher.publishEvent()`
- Files changed:
  - `DefaultBookingService.java`
  - `DefaultCargoInspectionService.java`
  - `DefaultHandlingEventService.java`

### Data Loading
- `InitLoader.java`: Changed from `@ApplicationScoped @Startup` to `@Component` with `@EventListener(ApplicationReadyEvent.class)`
- Modified `isSampleLoaded()` to programmatically create the `ApplicationSettings` row if it doesn't exist (replaced import.sql approach)
- `SampleDataGenerator.java`: `@ApplicationScoped` -> `@Service`

### ApplicationSettings Entity
- Added `getId()`/`setId()` methods to support programmatic creation

## Interfaces Layer

### REST Endpoints (JAX-RS -> Spring MVC)
- Replaced all JAX-RS annotations with Spring MVC equivalents:
  - `@Path` -> `@RequestMapping`
  - `@GET` -> `@GetMapping`
  - `@POST` -> `@PostMapping`
  - `@PUT` -> `@PutMapping`
  - `@PathParam` -> `@PathVariable`
  - `@QueryParam` -> `@RequestParam`
  - `@Produces` -> `produces` attribute on mapping annotation
  - `Response` -> `ResponseEntity`
- Removed JAXB `@XmlRootElement` annotations (Jackson doesn't need them)
- Files changed:
  - `CargoMonitoringService.java` (REST endpoint)
  - `HandlingReportService.java` (REST endpoint)
  - `GraphTraversalService.java` (pathfinder REST endpoint)
  - `TransitPath.java` (removed @XmlRootElement)
  - `TransitPaths.java` (removed @XmlRootElement)
  - `HandlingReport.java` (removed @XmlRootElement)

### SSE (Server-Sent Events)
- Replaced JAX-RS SSE (`SseEventSink`, `Sse`) with Spring `SseEmitter`
- `RealtimeCargoTrackingService.java`: Complete rewrite for Spring SSE

### JSF Web Beans -> Spring REST Controllers
- **Deleted all JSF backing beans** (9 files) and consolidated into REST controllers:
  - `BookingController.java` (NEW): Consolidates booking, cargo listing, tracking, itinerary selection, destination/deadline change functionality
    - `GET /rest/booking/locations` - list locations
    - `POST /rest/booking/cargo` - book new cargo
    - `GET /rest/booking/cargos` - list all cargos by routing status
    - `GET /rest/booking/cargo/{trackingId}` - cargo details
    - `GET /rest/booking/tracking/ids` - list tracking IDs
    - `GET /rest/booking/tracking/{trackingId}` - track cargo
    - `GET /rest/booking/cargo/{trackingId}/routes` - get route candidates
    - `POST /rest/booking/cargo/{trackingId}/assign-route` - assign route
    - `PUT /rest/booking/cargo/{trackingId}/destination` - change destination
    - `PUT /rest/booking/cargo/{trackingId}/deadline` - change deadline
  - `Track.java` (REWRITTEN): Public tracking REST controller
    - `GET /rest/public/track/{trackingId}` - public cargo tracking
  - `EventLogger.java` (REWRITTEN): Event logging REST controller
    - `GET /rest/event-logger/tracking-ids` - list tracking IDs
    - `GET /rest/event-logger/locations` - list locations
    - `GET /rest/event-logger/voyages` - list voyages
    - `GET /rest/event-logger/event-types` - list event types
    - `POST /rest/event-logger/register` - register handling event

- **Deleted JSF files:**
  - `Booking.java`, `Track.java` (booking), `ListCargo.java`, `CargoDetails.java`
  - `ItinerarySelection.java`, `ChangeDestination.java`, `ChangeDestinationDialog.java`
  - `ChangeArrivalDeadline.java`, `ChangeArrivalDeadlineDialog.java`
  - `FacesConfiguration.java`

### JSF XHTML/XML Files Deleted
- All XHTML templates under `META-INF/resources/` (booking, tracking, admin pages, templates)
- `META-INF/faces-config.xml`
- `META-INF/web.xml`
- Booking flow XML (`*-flow.xml`)

### Batch Processing (Jakarta Batch -> Spring Batch)
- `EventItemReader.java`: `extends AbstractItemReader` -> `implements ItemReader<HandlingEventRegistrationAttempt>`
- `EventItemWriter.java`: `extends AbstractItemWriter` -> `implements ItemWriter<HandlingEventRegistrationAttempt>`
- `FileProcessorJobListener.java`: `implements JobListener` -> `implements JobExecutionListener`
- `LineParseExceptionListener.java`: `implements SkipReadListener` -> `implements SkipListener`
- `UploadDirectoryScanner.java`: `io.quarkus.scheduler.Scheduled` -> Spring `@Scheduled(fixedRate=120000)`; `BatchRuntime` -> Spring `JobLauncher`
- `BatchJobConfig.java` (NEW): Spring Batch `@Configuration` defining job and step beans
- Deleted `META-INF/batch-jobs/EventFilesProcessorJob.xml`

## Test Layer

### Test Files Migrated
- `BookingServiceTest.java`: `@QuarkusTest` -> `@SpringBootTest`, `@Inject` -> `@Autowired`, Spring `@Transactional`
- `BookingServiceTestDataGenerator.java`: `@ApplicationScoped @Startup` -> `@Component @EventListener(ApplicationReadyEvent.class)`
- `TestDataGenerator.java`: `@ApplicationScoped` -> `@Service`, Spring `@Transactional`

## Smoke Tests

### smoke.py (NEW)
- 8 smoke tests covering key REST endpoints:
  1. List Locations (13 locations)
  2. List Cargos (by routing status)
  3. List Tracking IDs (4 IDs)
  4. Graph Traversal shortest-path (routes from CNHKG to FIHEL)
  5. Event Logger Tracking IDs
  6. Event Logger Locations
  7. Event Logger Voyages
  8. Event Types (5 types)
- All 8 tests pass

## Validation Results

- Maven compilation: SUCCESS
- Docker image build: SUCCESS
- Container startup: SUCCESS (7.9 seconds)
- Smoke tests: 8/8 PASS
