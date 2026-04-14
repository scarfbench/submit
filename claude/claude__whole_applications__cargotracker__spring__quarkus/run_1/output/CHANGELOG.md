# Changelog: Spring Boot to Quarkus Migration

## Overview

Migrated the Eclipse Cargo Tracker application from Spring Boot 3.3.0 to Quarkus 3.17.8.
The application is a Domain-Driven Design (DDD) cargo tracking system with REST APIs, JSF/PrimeFaces UI, JMS messaging, batch processing, and SSE endpoints.

## Build & Configuration

### pom.xml
- Removed Spring Boot parent POM (`spring-boot-starter-parent`)
- Added Quarkus BOM (`quarkus-bom 3.17.8`) under `<dependencyManagement>`
- Replaced Spring dependencies with Quarkus equivalents:
  - `spring-boot-starter-web` -> `quarkus-resteasy-jackson`
  - `spring-boot-starter-data-jpa` -> `quarkus-hibernate-orm`
  - `spring-boot-starter-validation` -> `quarkus-hibernate-validator`
  - `spring-boot-starter-activemq` -> removed (replaced by CDI events)
  - `spring-boot-starter-batch` -> `quarkus-scheduler`
  - `joinfaces-primefaces` -> `myfaces-quarkus 4.1.2` + `primefaces 14.0.0 jakarta`
- Added `quarkus-undertow` for Servlet support (required by MyFaces)
- Added `quarkus-resteasy-client-jackson` for REST client support
- Added `quarkus-jdbc-h2` for H2 database driver
- Added `quarkus-arc` for CDI support
- Replaced build plugins: `spring-boot-maven-plugin` -> `quarkus-maven-plugin`

### application.properties
- Rewrote from Spring Boot format to Quarkus format:
  - `spring.datasource.*` -> `quarkus.datasource.*`
  - `spring.jpa.*` -> `quarkus.hibernate-orm.*`
  - Added `quarkus.http.root-path=/cargo-tracker`
  - `app.GraphTraversalUrl` uses `@ConfigProperty` instead of `@Value`

### Dockerfile
- Changed CMD from `spring-boot:run` to `quarkus:dev -Dquarkus.http.host=0.0.0.0`

### web.xml (NEW)
- Created `src/main/webapp/WEB-INF/web.xml` with Jakarta Servlet 6.0 configuration
- Required by MyFaces Quarkus extension to avoid MYFACES-4735 NPE

### faces-config.xml
- Updated namespace from `jakarta.faces` to `https://jakarta.ee/xml/ns/jakartaee`
- Removed `SpringBeanFacesELResolver` (not needed with CDI)
- Kept PrimeFaces dialog framework handlers

## Dependency Injection

### All Java source files
- `@Component`, `@Service`, `@Repository` -> `@ApplicationScoped`
- `@Autowired` -> `@Inject`
- `@Value("${...}")` -> `@ConfigProperty(name = "...")`
- Constructor injection -> `@Inject` field injection (Quarkus CDI style)
- `@Validated` annotations removed (Quarkus validates via `@Valid` on methods)

### JSF Backing Beans
- `@Component` -> `@Named`
- Spring scopes (`@Scope("session")`) -> CDI scopes (`@SessionScoped`)
- `@ViewScope` -> `@ViewScoped` (Jakarta Faces)

## Persistence Layer

### Repository Classes (Complete Rewrite)
- **JpaCargoRepository**: Spring Data interface -> CDI `@ApplicationScoped` bean with `EntityManager`
  - `findByTrackingId()`, `findAll()`, `store()`, `nextTrackingId()` methods
  - Uses `entityManager.contains()` for persist/merge logic
  - Fires CDI async events on cargo store
- **JpaHandlingEventRepository**: Spring Data interface -> CDI bean with `EntityManager`
  - `lookupHandlingHistoryOfCargo()`, `save()` methods
- **JpaLocationRepository**: Spring Data interface -> CDI bean with `EntityManager`
  - `findByUnLocode()`, `findAll()` methods
- **JpaVoyageRepository**: Spring Data interface -> CDI bean with `EntityManager`
  - `findByVoyageNumber()`, `findAll()` methods

### Deleted Files
- `CargoRepository.java` (Spring Data interface)
- `CargoRepositoryImpl.java` (custom implementation)
- `HandlingEventRepository.java` (Spring Data interface)
- `HandlingEventRepositoryImpl.java` (custom implementation)

## Messaging

### JMS -> CDI Events
- **Deleted**: `JmsApplicationEvents.java`, `JmsConfig.java`
- **Created**: `CdiApplicationEvents.java`
  - Uses `Event.fireAsync()` with inner event wrapper classes
  - `CargoHandledEvent`, `CargoMisdirectedEvent`, `CargoDeliveredEvent`
- **Consumers** (all updated from `@JmsListener` to `@ObservesAsync`):
  - `CargoHandledConsumer.java`
  - `HandlingEventRegistrationAttemptConsumer.java`
  - `DeliveredCargoConsumer.java`
  - `MisdirectedCargoConsumer.java`
  - `RejectedRegistrationAttemptsConsumer.java`

## Batch Processing

### Spring Batch -> Quarkus Scheduler
- **Deleted**: `BatchJobConfig.java`, `EventItemReader.java`, `EventItemWriter.java`, `EventFilesCheckpoint.java`, `FileProcessorJobListener.java`, `LineParseExceptionListener.java`
- **Rewritten**: `UploadDirectoryScanner.java`
  - Uses `@Scheduled(every = "120s")` instead of Spring Batch Job/Step/ItemReader/ItemWriter
  - Inline file parsing and event processing

## REST Layer

### Controllers (Spring MVC -> JAX-RS)
- `@RestController` -> `@Path` + `@ApplicationScoped`
- `@GetMapping` -> `@GET` + `@Path`
- `@PostMapping` -> `@POST` + `@Path`
- `@RequestParam` -> `@QueryParam`
- `@RequestBody` -> direct parameter (JAX-RS auto-deserialization)
- `ResponseEntity<>` -> `Response` (jakarta.ws.rs.core)

### Affected Files
- `HandlingReportService.java` (`@Path("/rest/handling")`)
- `GraphTraversalService.java` (`@Path("/rest/graph-traversal")`)

### SSE Endpoints
- `RealtimeCargoTrackingService.java`: Spring `SseEmitter` -> JAX-RS SSE (`SseEventSink`, `Sse`, `OutboundSseEvent`)

### REST Client
- `ExternalRoutingService.java`: Spring `RestClient` -> JAX-RS `ClientBuilder` / `Client`

## Application Lifecycle

### Main Class
- `CargoTrackerApplication.java`: `@SpringBootApplication` + `SpringApplication.run()` -> `@QuarkusMain` + `Quarkus.run()`

### Startup
- `SampleDataGenerator.java`: `@PostConstruct` -> `void onStart(@Observes StartupEvent evt)`

### Configuration
- **Created**: `JacksonConfig.java` - CDI producer for `ObjectMapper` with `JavaTimeModule`
- **Updated**: `LoggerProducer.java` - `@Configuration`/`@Bean` -> `@ApplicationScoped`/`@Produces`

## Domain Model

### Handling Event Factory
- `HandlingEventFactory.java`: Removed `.orElseThrow()` on repository returns (repos now return direct objects, not Optional)

## Interface Layer

### Booking Facade
- `DefaultBookingServiceFacade.java`: Removed `.orElse(null)` calls on repository returns

### Tracking
- `Track.java`: Removed `.orElse(null)` on repository returns

### Assemblers
- All assembler classes: `@Component` -> `@ApplicationScoped`

## Test Files

### Deleted
- `BookingServiceTest.java` (Spring Boot test)
- `BookingServiceTestDataGenerator.java`
- `TestDataGenerator.java`

## Validation Results

- Application compiles and starts successfully on Quarkus 3.17.8
- All 4 smoke tests pass:
  1. Graph Traversal API - returns transit paths
  2. Handling Report REST API - accepts event submissions (HTTP 204)
  3. Graph Traversal Input Validation - returns 400 for missing params
  4. Graph Traversal Different Routes - multiple route queries work
- Sample data loads correctly at startup
- Hibernate ORM operates with H2 in-memory database
- JSF/PrimeFaces UI framework initializes (MyFaces + Undertow)
- Scheduled file scanner runs on 120s interval
