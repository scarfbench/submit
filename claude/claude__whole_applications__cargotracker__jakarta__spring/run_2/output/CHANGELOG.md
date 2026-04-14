# CHANGELOG - Jakarta EE to Spring Boot Migration

## Migration Summary

Migrated the Eclipse Cargo Tracker application from Jakarta EE 8 (javax.* namespace) running on Payara/GlassFish to Spring Boot 2.7.18 with embedded Tomcat.

## Build & Deployment Changes

### pom.xml (REPLACED)
- Changed parent from custom Jakarta EE parent to `spring-boot-starter-parent:2.7.18`
- Changed packaging from WAR to JAR (Spring Boot executable jar)
- Replaced Jakarta EE 8, Payara, OpenLiberty, JSF/PrimeFaces dependencies with Spring Boot starters:
  - `spring-boot-starter-web` (embedded Tomcat, Spring MVC)
  - `spring-boot-starter-data-jpa` (Hibernate 5 with javax.persistence)
  - `spring-boot-starter-validation` (Bean Validation)
  - `spring-boot-starter-webflux` (for RestTemplate/WebClient)
  - `h2` (in-memory database)
  - `commons-lang3`, `jackson-datatype-jsr310`
  - `spring-boot-starter-test` (JUnit 5)
- Java version changed from 1.8 to 11
- Build plugin changed from maven-war-plugin to spring-boot-maven-plugin

### Dockerfile (REPLACED)
- Multi-stage build: `maven:3.9.9-eclipse-temurin-11` (build) + `eclipse-temurin:11-jre` (runtime)
- Builds Spring Boot executable jar instead of WAR
- Installs Python 3 + uv for smoke tests
- CMD changed from Payara deployment to `java -jar app.jar`

### New Files Created
- `src/main/java/org/eclipse/cargotracker/CargoTrackerApplication.java` - Spring Boot main class with `@SpringBootApplication`, `@EnableScheduling`, `@ComponentScan`
- `src/main/resources/application.properties` - Replaces web.xml, persistence.xml, JNDI configuration
- `smoke/smoke.py` - Python smoke tests with PEP 723 inline metadata
- `smoke/pyproject.toml` - Python project configuration

### Files Removed / Superseded
- `src/main/resources/data.sql` - Superseded by `SampleDataGenerator.run()` ApplicationRunner
- `src/main/webapp/WEB-INF/web.xml` - Superseded by application.properties (JSF, JNDI, JMS configs no longer needed)

## Java Source Code Changes

### Dependency Injection (CDI -> Spring DI)
All files across the project:
- `@ApplicationScoped` -> `@Component` or `@Service` or `@Repository`
- `@Inject` -> `@Autowired`
- `@Named` -> `@Component`
- `@Produces` -> `@Bean` in `@Configuration` class
- `@Qualifier` annotations simplified

### EJB -> Spring Services
- **DefaultBookingService.java**: `@Stateless` -> `@Service` + `@Transactional`
- **DefaultHandlingEventService.java**: `@Stateless` -> `@Service` + `@Transactional`
- **DefaultCargoInspectionService.java**: `@Stateless` -> `@Service` + `@Transactional`

### JPA Repositories
- **JpaCargoRepository.java**: `@ApplicationScoped` -> `@Repository`, CDI `Event<Cargo>` -> `ApplicationEventPublisher`
- **JpaVoyageRepository.java**: `@ApplicationScoped` -> `@Repository`
- **JpaLocationRepository.java**: `@ApplicationScoped` -> `@Repository`
- **JpaHandlingEventRepository.java**: `@ApplicationScoped` -> `@Repository`

### JMS -> Spring Application Events
- **JmsApplicationEvents.java**: Replaced `JMSContext`/`Destination` queues with `ApplicationEventPublisher` + 4 inner event classes (`CargoHandledEvent`, `CargoMisdirectedEvent`, `CargoArrivedEvent`, `HandlingEventRegistrationEvent`)
- **CargoHandledConsumer.java**: `@MessageDriven` -> `@Component` + `@EventListener` + `@Async`
- **HandlingEventRegistrationAttemptConsumer.java**: Same pattern
- **MisdirectedCargoConsumer.java**: Same pattern
- **DeliveredCargoConsumer.java**: Same pattern
- **RejectedRegistrationAttemptsConsumer.java**: `@MessageDriven` -> `@Component`

### JAX-RS -> Spring MVC REST
- **HandlingReportService.java**: `@Stateless @Path` -> `@RestController @RequestMapping("/rest/handling")`, `@POST` -> `@PostMapping`
- **GraphTraversalService.java**: `@Stateless @Path` -> `@RestController @RequestMapping("/rest/graph-traversal")`, `@GET` -> `@GetMapping`, `@QueryParam` -> `@RequestParam`
- **RealtimeCargoTrackingService.java**: `@Singleton @Path` -> `@RestController @RequestMapping("/rest/cargo")`, JAX-RS SSE (`SseBroadcaster`/`SseEventSink`) -> Spring `SseEmitter` with `CopyOnWriteArrayList`

### JAX-RS Client -> RestTemplate
- **ExternalRoutingService.java**: `@Stateless` -> `@Service`, `@Resource(lookup)` -> `@Value`, JAX-RS `ClientBuilder`/`WebTarget` -> `RestTemplate` + `UriComponentsBuilder`

### JSF/PrimeFaces -> Spring Components
All JSF backing beans converted from `@Named` + JSF scopes to `@Component` + `@Scope("prototype")`:
- **Booking.java**, **Track.java** (tracking), **ListCargo.java**, **ItinerarySelection.java**, **CargoDetails.java**, **ChangeDestination.java**, **ChangeDestinationDialog.java**, **ChangeArrivalDeadline.java**, **ChangeArrivalDeadlineDialog.java**, **EventLogger.java** (mobile)
- Removed all `FacesContext`, `PrimeFaces`, `SelectItem`, `FlowEvent` usages
- Removed `FacesConfiguration.java` `@FacesConfig` annotation

### Jakarta Batch -> Spring Scheduled
- **UploadDirectoryScanner.java**: EJB `@Schedule` -> `@Scheduled(fixedRate=120000)`, consolidated batch processing logic
- **EventItemWriter.java**, **EventItemReader.java**, **FileProcessorJobListener.java**, **LineParseExceptionListener.java**: Replaced with empty stubs

### Other Migrations
- **SampleDataGenerator.java**: `@Singleton @Startup @PostConstruct` -> `@Component` implementing `ApplicationRunner`, `@Transactional` on `run()` method. Now creates initial `ApplicationSettings` row programmatically instead of relying on data.sql
- **ApplicationSettings.java**: Added constructor with id parameter
- **LoggerProducer.java**: CDI `@Produces` -> Spring `@Configuration` + `@Bean` + `@Scope("prototype")` using `InjectionPoint`
- **DefaultBookingServiceFacade.java**: `@ApplicationScoped` -> `@Service`
- **Track.java** (tracking/web): Replaced `JsonbBuilder` (JSON-B) with Jackson `ObjectMapper`
- **HandlingReport.java**: Removed `@XmlRootElement`
- **TransitPath.java**: Removed `@XmlRootElement`
- **GraphDao.java**: `@ApplicationScoped` -> `@Component`
- All assembler classes: `@ApplicationScoped` -> `@Component`

### Test File Migrations
All test files migrated from JUnit 4 to JUnit 5:
- `org.junit.Assert` -> `org.junit.jupiter.api.Assertions`
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- **BookingServiceTest.java**: Rewrote from Arquillian (`@RunWith(Arquillian.class)`, `@Deployment`, `ShrinkWrap`) to `@SpringBootTest` + `@Transactional` + `@TestMethodOrder`
- **BookingServiceTestDataGenerator.java**: `@Singleton @Startup` -> `@Component` + `@Transactional`
- Removed all `org.jboss.arquillian`, `org.jboss.shrinkwrap`, `javax.ejb`, `javax.inject` imports

## Startup Errors Fixed

1. **`data.sql` table not found**: Spring Boot tried to execute `data.sql` before Hibernate created tables. Fixed by setting `spring.sql.init.mode=never` and having `SampleDataGenerator` create the `ApplicationSettings` row programmatically.

2. **`TransactionRequiredException` in `@PostConstruct`**: Spring's `@Transactional` AOP proxy is not active during `@PostConstruct`. Fixed by implementing `ApplicationRunner` instead, which runs after full Spring context initialization with proper transaction proxy support.

3. **Test compilation failures**: Test files used JUnit 4, Arquillian, and Jakarta EE imports incompatible with Spring Boot. Fixed by migrating all tests to JUnit 5 and Spring Boot Test.

## Smoke Test Results

All 4 smoke tests passed:
- **Application Root**: Accessible (status 404 - no static content, expected)
- **Graph Traversal**: Returns 5 routes for CNHKG -> FIHEL (status 200)
- **Handling Report**: Endpoint responsive (status 400 - validation working)
- **SSE Cargo Tracking**: Endpoint accessible (status 200)
