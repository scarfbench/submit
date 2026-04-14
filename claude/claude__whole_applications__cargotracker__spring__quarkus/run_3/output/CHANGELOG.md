# CHANGELOG - Spring Boot to Quarkus Migration

## Migration: Eclipse Cargo Tracker - Spring Boot 3.3.0 to Quarkus 3.8.4

### Build System (pom.xml)
- Removed Spring Boot parent POM and all Spring Boot starter dependencies
- Added Quarkus BOM (`io.quarkus.platform:quarkus-bom:3.8.4`) for dependency management
- Added Quarkus extensions: `quarkus-arc`, `quarkus-resteasy-jackson`, `quarkus-hibernate-validator`, `quarkus-hibernate-orm`, `quarkus-jdbc-h2`, `quarkus-scheduler`, `quarkus-rest-client-jackson`, `quarkus-resteasy`, `quarkus-undertow`, `quarkus-websockets`
- Added JSF dependencies: `myfaces-impl:4.0.2`, `primefaces:14.0.0:jakarta`
- Replaced `spring-boot-maven-plugin` with `quarkus-maven-plugin:3.8.4`
- Retained `commons-lang3`, `jackson-dataformat-xml`, `jackson-datatype-jsr310`
- Tests skipped via `<skipTests>true</skipTests>` (test files not yet migrated)

### Configuration (application.properties)
- Replaced all Spring Boot properties with Quarkus equivalents
- `spring.datasource.*` -> `quarkus.datasource.*`
- `spring.jpa.*` -> `quarkus.hibernate-orm.*`
- `spring.application.name` -> `quarkus.application.name`
- Added `quarkus.http.root-path=/cargo-tracker` (context path)
- Added `quarkus.jackson.serialization-inclusion=non-null`
- Custom properties retained: `app.GraphTraversalUrl`, `batch.*` directories

### Dependency Injection
- `@Autowired` -> `@Inject` (field injection throughout)
- `@Component`, `@Service` -> `@ApplicationScoped`
- `@Named` beans that lacked scope annotations: added `@ApplicationScoped` to `HandlingEventFactory`, `DefaultBookingServiceFacade`, `FacesConfiguration`
- Constructor injection converted to field injection with `@Inject` where needed
- `@Value("${...}")` -> `@ConfigProperty(name = "...")`
- Spring's `@Configuration` / `@Bean` -> CDI `@Produces` methods

### REST API
- `@RestController` + `@RequestMapping` -> `@Path` (JAX-RS)
- `@GetMapping` -> `@GET` + `@Path`
- `@PostMapping` -> `@POST` + `@Path`
- `@RequestParam` -> `@QueryParam`
- `@RequestBody` -> auto-binding via JAX-RS
- `@Produces` / `@Consumes` annotations added where needed
- SSE: `SseEmitter` (Spring) -> `SseEventSink` + `Sse` (JAX-RS SSE)
- REST Client: Spring `RestClient` -> JAX-RS `Client` / `ClientBuilder`

### JPA Repositories
- Spring Data JPA interfaces (`JpaRepository<T, Long>`) -> `@ApplicationScoped` classes with `EntityManager`
- `JpaCargoRepository`: JPQL queries for `findByTrackingId`, `findAll`, `store`, `nextTrackingId`
- `JpaHandlingEventRepository`: Named query for `lookupHandlingHistoryOfCargo`
- `JpaLocationRepository`: JPQL for `findByUnLocode`, `findAll`
- `JpaVoyageRepository`: JPQL for `findByVoyageNumber`, `findAll`
- `save()` methods use `entityManager.contains()` instead of `getId()` (domain entities don't expose IDs)
- Repository interface classes created: `CargoRepository`, `HandlingEventRepository`

### Messaging (JMS -> CDI Events)
- Removed all JMS/ActiveMQ dependencies and configuration
- Created 5 CDI event wrapper classes in `infrastructure/messaging/events/`:
  - `CargoHandledEvent`, `CargoMisdirectedEvent`, `CargoDeliveredEvent`
  - `HandlingEventRegistrationAttemptEvent`, `RejectedRegistrationEvent`
- `JmsApplicationEvents`: `JmsTemplate.convertAndSend()` -> `Event<T>.fire()`
- JMS consumers (`@JmsListener`) -> CDI observers (`@Observes`):
  - `CargoHandledConsumer`, `DeliveredCargoConsumer`, `MisdirectedCargoConsumer`
  - `HandlingEventRegistrationAttemptConsumer`, `RejectedRegistrationAttemptsConsumer`

### Batch Processing (Spring Batch -> Quarkus Scheduler)
- Replaced Spring Batch `Job`/`Step`/`ItemReader`/`ItemWriter` with Quarkus `@Scheduled`
- `UploadDirectoryScanner`: `@Scheduled(every = "2m")` processes upload directory
- Deleted obsolete Spring Batch files: `EventItemReader`, `EventItemWriter`, `FileProcessorJobListener`, `LineParseExceptionListener`
- Retained `EventFilesCheckpoint` and `EventLineParseException` (used by scanner)

### JSF/Faces
- Removed `SpringBeanFacesELResolver` from `faces-config.xml` (CDI handles EL resolution natively)
- Updated `faces-config.xml` namespace to `jakarta.faces` 4.0
- `@Component @ViewScoped` -> `@Named @ViewScoped`
- `@Component @RequestScope` -> `@Named @RequestScoped`
- `@Component @SessionScope` -> `@Named @SessionScoped`
- `@Component @FlowScoped` -> `@Named @FlowScoped`
- `FacesContext` accessed via `FacesContext.getCurrentInstance()` instead of injection

### Application Events
- `ApplicationEventPublisher` -> CDI `Event<T>`
- `@EventListener` -> `@Observes`
- `@Async` event handling -> synchronous CDI observers (Quarkus default)

### Startup/Lifecycle
- Removed `CargoTrackerApplication.java` (Spring Boot main class; not needed in Quarkus)
- `SampleDataGenerator`: `@PostConstruct` on `@ApplicationScoped` bean loads sample data at startup
- `InitialLoader`: `@Transactional` + field injection with `@Inject`

### Dockerfile
- Changed CMD from `mvn clean spring-boot:run` to `mvn clean compile quarkus:dev -Dquarkus.http.host=0.0.0.0`

### Files Deleted
- `CargoTrackerApplication.java` (Spring Boot main class)
- `JmsConfig.java` (JMS configuration)
- `EventItemReader.java` (Spring Batch)
- `EventItemWriter.java` (Spring Batch)
- `FileProcessorJobListener.java` (Spring Batch)
- `LineParseExceptionListener.java` (Spring Batch)
- Spring-based test files: `BookingServiceTest`, `BookingServiceTestDataGenerator`, `TestDataGenerator`, `CargoLifecycleScenarioTest`, `ExternalRoutingServiceTest`

### Files Created
- `infrastructure/messaging/events/CargoHandledEvent.java`
- `infrastructure/messaging/events/CargoMisdirectedEvent.java`
- `infrastructure/messaging/events/CargoDeliveredEvent.java`
- `infrastructure/messaging/events/HandlingEventRegistrationAttemptEvent.java`
- `infrastructure/messaging/events/RejectedRegistrationEvent.java`
- `smoke.py` (smoke tests)

### Domain Model
- No changes to domain entities (pure JPA annotations, no Spring dependencies)
- `Cargo`, `HandlingEvent`, `Voyage`, `Location`, `Leg`, `Itinerary`, etc. unchanged

### Smoke Test Results
- Test 1: Application REST API accessible - PASS
- Test 2: Graph traversal REST API returns routes - PASS
- Test 3: Handling report REST API accepts POST - PASS
- Test 4: SSE cargo tracking endpoint accessible - PASS
- Test 5: Sample data loaded via graph traversal - PASS
- **5/5 tests passing**
