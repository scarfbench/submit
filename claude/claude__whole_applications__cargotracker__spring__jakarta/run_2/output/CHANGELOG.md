# Migration Changelog: Spring Boot 3.3.0 to Jakarta EE 10 (Payara Micro)

## Summary

Migrated the Eclipse Cargo Tracker application from Spring Boot 3.3.0 to Jakarta EE 10 running on Payara Micro 6.2024.6. This involved rewriting all infrastructure, persistence, messaging, and web layers from Spring annotations/conventions to Jakarta EE equivalents.

## Build System Changes

### pom.xml
- Changed packaging from Spring Boot executable JAR to standard WAR
- Removed all Spring Boot dependencies (`spring-boot-starter-*`)
- Added `jakarta.platform:jakarta.jakartaee-api:10.0.0` (provided scope)
- Added `org.primefaces:primefaces:13.0.0` (jakarta classifier)
- Retained `com.h2database:h2:2.2.224`, `jackson-databind`, `commons-lang3`

### Dockerfile
- Replaced Spring Boot `java -jar` startup with Payara Micro deployment
- Downloads `payara-micro-6.2024.6.jar` at build time
- Builds WAR via `mvn clean package -DskipTests`
- CMD: `java -jar /opt/payara-micro.jar --deploy cargo-tracker.war --port 8080 --contextroot cargo-tracker`
- Added `requests` to Python venv for smoke tests

## Deleted Spring-Specific Files
- `CargoTrackerApplication.java` (Spring Boot main class)
- `JmsConfig.java` (Spring JMS configuration)
- `application.properties` (Spring configuration)

## Infrastructure Layer Changes

### Persistence (JPA Repositories)
- Rewrote 4 repository implementations from Spring Data JPA (`@Repository`, `JpaRepository`) to Jakarta EE (`@ApplicationScoped`, `@PersistenceContext EntityManager`)
  - `JpaCargoRepository` -> direct EntityManager JPQL queries
  - `JpaHandlingEventRepository` -> direct EntityManager JPQL queries
  - `JpaLocationRepository` -> direct EntityManager JPQL queries
  - `JpaVoyageRepository` -> direct EntityManager JPQL queries
- Repository interfaces kept as plain Java interfaces (CDI injection via `@ApplicationScoped` implementations)

### Messaging (JMS -> CDI Events)
- Replaced all JMS messaging with CDI Events (Payara Micro doesn't include full JMS RA)
- Created 3 CDI event wrapper classes:
  - `CargoHandledEvent` - fired when cargo handling is recorded
  - `CargoMisdirectedEvent` - fired when cargo is detected as misdirected
  - `CargoDeliveredEvent` - fired when cargo arrives at destination
- Rewrote `JmsApplicationEvents` to use `Event<T>.fire()` instead of `JMSContext.createProducer().send()`
- Rewrote 5 Message-Driven Beans to CDI Observers:
  - `CargoHandledConsumer`: `@MessageDriven` -> `@ApplicationScoped` + `@Observes CargoHandledEvent`
  - `MisdirectedCargoConsumer`: `@MessageDriven` -> `@ApplicationScoped` + `@Observes CargoMisdirectedEvent`
  - `DeliveredCargoConsumer`: `@MessageDriven` -> `@ApplicationScoped` + `@Observes CargoDeliveredEvent`
  - `HandlingEventRegistrationAttemptConsumer`: `@MessageDriven` -> `@ApplicationScoped` + `@Observes HandlingEventRegistrationAttempt`
  - `RejectedRegistrationAttemptsConsumer`: `@MessageDriven` -> `@ApplicationScoped` + `@Observes HandlingEventRegistrationAttempt`

### REST (JAX-RS)
- Added `JaxRsActivator.java` (`@ApplicationPath("/rest")`) for JAX-RS activation
- REST resources use standard `jakarta.ws.rs` annotations (no Spring `@RestController`)

### CDI Producers
- Added `LoggerProducer` for `@Inject Logger` support via CDI `@Produces`

## Application Layer Changes

### Services
- All service classes use `@ApplicationScoped` + `@Inject` instead of Spring `@Service` + `@Autowired`
- `DefaultBookingService`, `DefaultCargoInspectionService`, `DefaultHandlingEventService` all rewired

### Facades
- `DefaultBookingServiceFacade` - added missing `listAllTrackingIds()` implementation

## Web Layer Changes

### JSF Configuration
- `faces-config.xml`: Updated to Jakarta Faces 4.0 namespace, removed `SpringBeanFacesELResolver`
- `booking-flow.xml`: Updated from JavaEE 2.3 to Jakarta 4.0 namespace
- `web.xml`: Updated to Jakarta EE Web 6.0 namespace with Faces Servlet mapping

### JSF Page Locations
- Moved all XHTML files from `src/main/resources/META-INF/resources/` (Spring Boot convention) to `src/main/webapp/` (standard WAR convention)
- Pages: index.xhtml, admin/*, booking/*, public/*, event-logger/*, templates/*
- Static resources: css/, js/, images/, leaflet/

### Backing Beans
- All JSF backing beans use `@Named` + `@ViewScoped`/`@FlowScoped` (Jakarta CDI) instead of Spring-managed beans

## Configuration Files

### persistence.xml
- Namespace: `https://jakarta.ee/xml/ns/persistence` (version 3.0)
- JPA provider: EclipseLink (Payara's default, replaces Hibernate)
- Schema generation: `drop-and-create`

### glassfish-resources.xml
- JDBC connection pool with H2 in-memory database (`jdbc:h2:mem:cargo-tracker-database;DB_CLOSE_DELAY=-1`)
- JNDI: `java:app/jdbc/CargoTrackerDatabase`
- Removed all JMS queue definitions

### beans.xml
- Jakarta CDI 4.0, `bean-discovery-mode="all"`

## Data Initialization

### SampleDataGenerator
- Rewrote to build `Itinerary`/`Leg` objects directly instead of calling REST routing service at startup
- Added `loc()` and `voy()` helper methods to look up JPA-managed entities from the database
- Fixed `HandlingEvent` construction: uses 5-arg constructor (no voyage) for RECEIVE/CUSTOMS/CLAIM events, 6-arg constructor (with voyage) for LOAD/UNLOAD events

### InitialLoader
- `@Singleton @Startup` EJB that calls `SampleDataGenerator.loadSampleData()` on deployment

## Smoke Test Results (6/6 passed)
- Application startup: PASS
- Index page (200): PASS
- Graph traversal REST endpoint (4 routes): PASS
- Handling report validation (400 on invalid data): PASS
- Handling report submission (204): PASS
- SSE cargo endpoint: PASS
- Dashboard JSF page (200): PASS
- Public tracking JSF page (200): PASS
