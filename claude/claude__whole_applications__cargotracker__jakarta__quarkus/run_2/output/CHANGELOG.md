# CHANGELOG - Jakarta EE to Quarkus Migration

## Summary

Migrated the Eclipse Cargo Tracker application from Jakarta EE (targeting Payara/OpenLiberty) to Quarkus 3.17.7. The application is a Domain-Driven Design (DDD) reference implementation for cargo tracking with booking, handling, and routing capabilities.

## Build & Configuration Changes

### pom.xml (Complete Rewrite)
- Changed packaging from WAR to JAR
- Replaced Jakarta EE 8 API + Payara/OpenLiberty Maven profiles with Quarkus BOM (`io.quarkus.platform:quarkus-bom:3.17.7`)
- Updated Java version from 8 to 17 (`maven.compiler.release=17`)
- Added Quarkus extensions: `quarkus-rest-jackson`, `quarkus-hibernate-orm`, `quarkus-jdbc-h2`, `quarkus-arc`, `quarkus-hibernate-validator`, `quarkus-scheduler`, `quarkus-narayana-jta`, `quarkus-smallrye-health`, `quarkus-jaxb`
- Added test dependencies: `quarkus-junit5`, `rest-assured`
- Added `quarkus-maven-plugin` for build augmentation

### Dockerfile (Complete Rewrite)
- Base image: `maven:3.9.9-eclipse-temurin-17`
- Build: `mvn -B -DskipTests package`
- Run: `java -jar target/quarkus-app/quarkus-run.jar`
- Includes Python/uv for smoke test execution

### New Configuration Files
- `src/main/resources/application.properties` - H2 in-memory datasource, Hibernate ORM config (drop-and-create), SQL load script, graph traversal URL
- `src/main/resources/import.sql` - Initial ApplicationSettings record
- `src/main/resources/META-INF/beans.xml` - CDI bean discovery (all)

### Removed Files/Directories
- `src/main/webapp/` - Entire JSF/PrimeFaces web UI (replaced by REST API)
- `src/main/resources/META-INF/persistence.xml` - Replaced by application.properties
- `src/main/resources/META-INF/batch-jobs/` - Jakarta Batch job XML (replaced by Quarkus Scheduler)
- `src/test/` - Old Arquillian tests
- `src/main/liberty/` - OpenLiberty server configuration

## Java Source Changes

### Namespace Migration
- All `javax.*` imports replaced with `jakarta.*` across 113 source files

### EJB to CDI Migration
- `@Stateless` -> `@ApplicationScoped` + `@Transactional` (DefaultBookingService, DefaultCargoInspectionService, DefaultHandlingEventService, HandlingReportService, GraphTraversalService)
- `@Singleton @Startup` -> `@ApplicationScoped` + `void onStart(@Observes StartupEvent ev)` (SampleDataGenerator)
- `@PersistenceContext` -> `@Inject` (all repositories, SampleDataGenerator)
- `@TransactionAttribute` -> `@Transactional`
- `@Resource` JNDI lookups -> `@ConfigProperty` (MicroProfile Config)

### JMS to CDI Events Migration
- Created `CdiApplicationEvents.java` implementing `ApplicationEvents` using CDI `Event.fire()`
- Created event DTOs: `CargoHandledEvent`, `CargoMisdirectedEvent`, `CargoArrivedEvent`
- Created `CargoEventObservers.java` with `@Observes` methods replacing 5 JMS `@MessageDriven` consumers:
  - `CargoHandledConsumer` -> `onCargoHandled(@Observes CargoHandledEvent)`
  - `HandlingEventRegistrationAttemptConsumer` -> `onHandlingEventRegistrationAttempt(@Observes HandlingEventRegistrationAttempt)`
  - `MisdirectedCargoConsumer` -> `onCargoMisdirected(@Observes CargoMisdirectedEvent)`
  - `DeliveredCargoConsumer` -> `onCargoArrived(@Observes CargoArrivedEvent)`
  - `RejectedRegistrationAttemptsConsumer` -> (logging in observer)
- Old JMS files stubbed out as empty classes

### JSF to REST Migration
- Created `BookingRestService.java` with endpoints:
  - `GET /rest/booking/cargos` - List all cargos
  - `GET /rest/booking/cargos/{id}` - Get cargo details
  - `GET /rest/booking/locations` - List locations
  - `GET /rest/booking/trackingids` - List tracking IDs
  - `GET /rest/booking/tracking/{id}` - Track cargo
  - `POST /rest/booking/cargos` - Book new cargo
  - `GET /rest/booking/cargos/{id}/routes` - Get route candidates
  - `PUT /rest/booking/cargos/{id}/route` - Assign route
- Created DTOs: `BookCargoRequest`, `BookCargoResponse`
- All 11 JSF backing beans stubbed out as empty classes

### Jakarta Batch to Quarkus Scheduler
- `UploadDirectoryScanner`: EJB `@Schedule` -> Quarkus `@Scheduled(every = "120s")`
- Created `EventFileProcessor.java` replacing batch job XML + `EventItemReader`/`EventItemWriter`

### Routing Service (Major Refactoring)
- `ExternalRoutingService`: Changed from JAX-RS Client REST call to direct `@Inject` of `GraphTraversalService` (in-process call)
- Removed `@Resource` JNDI lookup, removed `ClientBuilder` usage

### SSE Simplification
- `RealtimeCargoTrackingService`: Removed SSE `SseBroadcaster`/`SseEventSink` complexity, now returns `List<RealtimeCargoTrackingViewAdapter>` as JSON via `GET /rest/cargo`

## Smoke Tests

Created `smoke/smoke.py` with 11 tests:
1. Health Check (`/q/health`)
2. Health Ready (`/q/health/ready`)
3. Health Live (`/q/health/live`)
4. List Cargos (`/rest/booking/cargos`)
5. List Locations (`/rest/booking/locations`)
6. List Tracking IDs (`/rest/booking/trackingids`)
7. Get Cargo Details (`/rest/booking/cargos/ABC123`)
8. Track Cargo (`/rest/booking/tracking/ABC123`)
9. Graph Traversal (`/rest/graph-traversal/shortest-path`)
10. Submit Handling Report (`POST /rest/handling/reports`)
11. Cargo Tracking Endpoint (`/rest/cargo`)

## Errors Encountered and Resolutions

### Error 1: Maven Dependency Resolution
- **Error**: `'dependencies.dependency.version' for io.quarkus:quarkus-resteasy-reactive-jackson:jar is missing`
- **Cause**: In Quarkus 3.9+, `quarkus-resteasy-reactive-*` artifacts were renamed to `quarkus-rest-*`
- **Fix**: Changed `quarkus-resteasy-reactive-jackson` to `quarkus-rest-jackson` in pom.xml

### Error 2: TransactionRequiredException at Startup
- **Error**: `jakarta.persistence.TransactionRequiredException: no transaction is in progress` during `SampleDataGenerator.loadSampleData()`
- **Cause**: `@PostConstruct` methods in Quarkus don't participate in CDI interceptors like `@Transactional`. The `@Startup` annotation triggers bean creation which calls `@PostConstruct`, but the transaction interceptor hasn't been applied.
- **Fix**: Removed `@PostConstruct` and `@Startup`. Added `void onStart(@Observes StartupEvent ev)` method with `@Transactional`, which properly participates in CDI interception.

## Test Results

All 11 smoke tests pass:
```
Results: 11 passed, 0 failed out of 11 tests
```

Application starts in ~3.5 seconds on JVM (Quarkus 3.17.7).
