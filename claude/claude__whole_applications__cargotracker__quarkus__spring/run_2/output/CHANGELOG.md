# CHANGELOG

## [1.0.0] - Quarkus to Spring Boot Migration

### Overview
Complete migration of the Eclipse CargoTracker DDD sample application from Quarkus 3.x to Spring Boot 3.2.5. The application retains all domain logic, sample data, REST APIs, and JMS messaging while replacing the entire Quarkus infrastructure with Spring Boot equivalents.

### Dependencies Changed

**Removed (Quarkus):**
- `quarkus-bom` (platform BOM)
- `quarkus-rest-jackson`, `quarkus-rest-client-jackson`, `quarkus-rest-jaxb` (JAX-RS)
- `quarkus-hibernate-orm-panache`, `quarkus-hibernate-orm` (ORM)
- `quarkus-jdbc-h2` (datasource)
- `quarkus-qpid-jms` (JMS via AMQP)
- `quarkus-hibernate-validator` (validation)
- `quarkus-primefaces` (JSF UI)
- `quarkus-scheduler` (scheduling)
- `quarkus-jberet` (batch processing)
- `quarkus-arc` (CDI)
- `quarkus-junit5`, `rest-assured` (testing)

**Added (Spring Boot):**
- `spring-boot-starter-parent:3.2.5` (parent POM)
- `spring-boot-starter-web` (Spring MVC, embedded Tomcat)
- `spring-boot-starter-data-jpa` (JPA with Hibernate)
- `spring-boot-starter-validation` (Bean Validation)
- `spring-boot-starter-actuator` (health/info endpoints)
- `spring-boot-starter-artemis` (JMS via ActiveMQ Artemis)
- `h2` (in-memory database)
- `jackson-datatype-jsr310` (Java 8 date/time serialization)
- `commons-lang3` (Apache Commons utilities used by domain model)
- `jakarta.xml.bind-api` (JAXB annotations for XML-bound DTOs)
- `spring-boot-starter-test` (testing)

### Configuration Changes

- **application.properties**: Rewritten from Quarkus format to Spring Boot format
  - `server.servlet.context-path=/cargo-tracker` (context path)
  - `spring.datasource.*` (H2 in-memory datasource)
  - `spring.jpa.hibernate.ddl-auto=create-drop` (schema generation)
  - `spring.artemis.broker-url=tcp://localhost:61616` (JMS broker, changed from AMQP port 5672)
  - `management.endpoints.web.exposure.include=health,info` (actuator)
- **import.sql**: Updated table/column names to match Hibernate default snake_case naming strategy

### Code Changes

#### New Files
- `CargoTrackerApplication.java` - Spring Boot main class with `@SpringBootApplication`, `@EnableJms`, `@EnableScheduling`
- `BookingRestController.java` - New REST controller exposing booking facade as JSON API (replaces JSF-based booking UI)

#### Annotation/Import Migrations (Applied Across All Layers)
| Quarkus / Jakarta EE | Spring Boot |
|---|---|
| `@ApplicationScoped` | `@Service` / `@Component` / `@Repository` |
| `@Inject` | `@Autowired` |
| `@ConfigProperty(name=...)` | `@Value("${...}")` |
| `jakarta.transaction.Transactional` | `org.springframework.transaction.annotation.Transactional` |
| `PanacheRepositoryBase<T, ID>` | `@PersistenceContext EntityManager` |
| `Event<T>` (CDI events) | `ApplicationEventPublisher` |
| `@Path` / `@GET` / `@POST` / `@Produces` | `@RestController` / `@GetMapping` / `@PostMapping` |
| `@Observes StartupEvent` | `@EventListener(ApplicationReadyEvent.class)` |
| `ConnectionFactory` / `JMSContext` | `JmsTemplate` / `@JmsListener` |
| `@Startup` + `@UnlessBuildProfile("test")` | `@EventListener(ApplicationReadyEvent.class)` |

#### Infrastructure Layer
- **JPA Repositories** (`JpaCargoRepository`, `JpaHandlingEventRepository`, `JpaLocationRepository`, `JpaVoyageRepository`): Replaced Panache `PanacheRepositoryBase` with direct `EntityManager` usage via `@PersistenceContext`. Added `findAll()` implementations.
- **Repository Interfaces** (`CargoRepository`, `LocationRepository`, `VoyageRepository`): Added `findAll()` method signatures.
- **JmsApplicationEvents**: Replaced manual `JMSContext.createProducer()` with Spring `JmsTemplate.convertAndSend()`.
- **JMS Consumers** (`CargoHandledConsumer`, `HandlingEventRegistrationAttemptConsumer`, `DeliveredCargoConsumer`, `MisdirectedCargoConsumer`, `RejectedRegistrationAttemptsConsumer`): Replaced `@MessageDriven` / manual `ConnectionFactory` with Spring `@JmsListener`.
- **LoggerProducer**: Changed from CDI `@Produces` to Spring `@Configuration` with `@Bean @Scope("prototype")`.
- **ExternalRoutingService**: Replaced JAX-RS `WebTarget` (REST client) with Spring `RestTemplate`.

#### Application Layer
- **DefaultBookingService**, **DefaultCargoInspectionService**, **DefaultHandlingEventService**: Migrated to `@Service` with Spring `@Transactional`.
- **SampleDataGenerator**: Changed from `@Observes StartupEvent` to `@EventListener(ApplicationReadyEvent.class)`.
- **InitLoader**: Converted to `@Component` with `@PersistenceContext` and Spring `@Transactional`.
- **HandlingEventFactory**: Converted to `@Component` with `@Autowired`.

#### Interfaces Layer
- **REST Endpoints** (`HandlingReportService`, `GraphTraversalService`): Converted from JAX-RS `@Path`/`@GET`/`@POST` to Spring MVC `@RestController`/`@GetMapping`/`@PostMapping`.
- **RealtimeCargoTrackingService**: Replaced JAX-RS SSE (`SseEventSink`/`Sse`) with Spring `SseEmitter`.
- **DefaultBookingServiceFacade**: Converted to `@Service`, updated repository calls to use `findAll()`.
- **DTO Assemblers**: Converted to `@Component` with `@Autowired`.
- **JSF Backing Beans** (9+ classes): Stripped JSF annotations (`@Named`, `@ViewScoped`, `@FacesConfig`), converted to plain `@Component` POJOs.
- **File Processing Classes** (7 classes): Removed JBeret/Jakarta Batch annotations, converted to `@Component`.

#### Domain Layer
- **No changes** to domain model classes (they use standard JPA annotations compatible with both Quarkus and Spring Boot).

### Removed Files
- `src/main/resources/META-INF/resources/` (JSF/XHTML templates and Primefaces resources)
- `src/main/resources/META-INF/batch-jobs/` (JBeret batch job XML)
- `src/main/resources/META-INF/faces-config.xml` (JSF configuration)
- `src/main/docker/` (Quarkus-specific Dockerfiles)
- `src/test/` (Quarkus test files)

### Dockerfile
- Build command changed from `mvn clean install -DskipTests` + `mvn quarkus:run` to `mvn clean package -DskipTests` + `java -jar target/cargo-tracker-1.0.0-SNAPSHOT.jar`
- Artemis broker startup retained (embedded broker started before application)

### Validation
- Docker image builds successfully
- Application starts in ~10 seconds
- All 7 smoke tests pass:
  - Health endpoint (actuator) returns UP
  - List locations returns 13 locations
  - List cargos returns 4 sample cargos
  - Get cargo details for ABC123
  - List tracking IDs includes ABC123
  - Graph traversal returns route candidates
  - Handling event report submission succeeds
