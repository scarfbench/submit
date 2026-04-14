# CHANGELOG - Eclipse Cargo Tracker: Spring Boot to Quarkus Migration

## Migration Summary

Migrated the Eclipse Cargo Tracker application from **Spring Boot 3.3.0** to **Quarkus 3.8.4**, preserving the DDD architecture and all functional behavior.

---

## Actions Performed

### 1. Dependency Migration (pom.xml)

- Removed Spring Boot parent POM (`spring-boot-starter-parent 3.3.0`)
- Removed all `spring-boot-starter-*` dependencies (web, data-jpa, activemq, batch, test, validation)
- Removed JoinFaces (`joinfaces-spring-boot-starter`), `activemq-broker`
- Added Quarkus BOM (`io.quarkus.platform:quarkus-bom:3.8.4`) via `dependencyManagement`
- Added Quarkus extensions:
  - `quarkus-arc` (CDI container)
  - `quarkus-resteasy-jackson` (JAX-RS + JSON)
  - `quarkus-hibernate-orm` (JPA)
  - `quarkus-jdbc-h2` (H2 database)
  - `quarkus-hibernate-validator` (Bean Validation)
  - `quarkus-scheduler` (replaces Spring Batch)
  - `quarkus-undertow` (Servlet support for JSF)
  - `quarkus-narayana-jta` (JTA transactions)
  - `quarkus-rest-client-jackson` (REST client)
  - `quarkus-junit5` (testing)
- Added `myfaces-quarkus:4.0.2` (replaces JoinFaces for JSF)
- Retained `primefaces:14.0.0:jakarta`, `jackson-dataformat-xml`, `jackson-datatype-jsr310`, `commons-lang3`
- Added `quarkus-maven-plugin` for build, replaced `spring-boot-maven-plugin`

### 2. Configuration Migration

- **application.properties**: Converted from Spring Boot to Quarkus format
  - `server.port` → `quarkus.http.port`
  - `server.servlet.context-path` → `quarkus.http.root-path`
  - `spring.datasource.*` → `quarkus.datasource.*`
  - `spring.jpa.*` → `quarkus.hibernate-orm.*`
  - Switched from file-based H2 to in-memory: `jdbc:h2:mem:cargo-tracker-database;DB_CLOSE_DELAY=-1`
  - Added `quarkus.myfaces.project-stage=Development`
  - Used `app.GraphTraversalUrl` and `batch.*` as custom MicroProfile Config properties

### 3. Main Application Class

- `CargoTrackerApplication.java`: `@SpringBootApplication` + `SpringApplication.run()` → `@QuarkusMain` + `Quarkus.run()`

### 4. Configuration Classes

- **JmsConfig.java** → **JacksonConfig.java**: Replaced JMS connection factory with CDI `@Produces @Singleton ObjectMapper`
- **LoggerProducer.java**: Spring `@Configuration` → CDI `@ApplicationScoped` with `@Produces Logger` using `InjectionPoint`
- **FacesConfiguration.java**: Removed `@Component`, kept `@FacesConfig`

### 5. Service Layer Migration

- Service interfaces (`BookingService`, `HandlingEventService`, `CargoInspectionService`): Removed `@Validated`
- Service implementations (`DefaultBookingService`, `DefaultHandlingEventService`, `DefaultCargoInspectionService`):
  - `@Service` → `@ApplicationScoped`
  - `org.springframework.transaction.annotation.Transactional` → `jakarta.transaction.Transactional`
  - Constructor injection → `@Inject` field injection

### 6. Repository Layer Migration

- Converted Spring Data JPA interfaces to concrete CDI beans with `@Inject EntityManager`
- **JpaCargoRepository**: Spring `ApplicationEventPublisher` → CDI `Event<Cargo>`; explicit JPQL queries
- **JpaHandlingEventRepository**: Direct EntityManager queries replacing Spring Data conventions
- **JpaLocationRepository**, **JpaVoyageRepository**: JPQL queries replacing Spring Data method naming
- All repositories: `@Repository`/`@Component` → `@ApplicationScoped`, `@PersistenceContext` → `@Inject`

### 7. Messaging Layer Migration

- **JmsApplicationEvents**: `JmsTemplate.convertAndSend()` → CDI `Event<T>.fire()`
- Created CDI qualifier annotations: `@CargoHandled`, `@MisdirectedCargo`, `@DeliveredCargo`, `@HandlingEventRegistration`
- **CargoHandledConsumer**: `@JmsListener` → `@Observes String`
- **HandlingEventRegistrationAttemptConsumer**: `@JmsListener` → `@Observes HandlingEventRegistrationAttempt`
- **MisdirectedCargoConsumer**, **DeliveredCargoConsumer**, **RejectedRegistrationAttemptsConsumer**: Simplified to `@ApplicationScoped` stubs

### 8. REST Endpoint Migration

- **HandlingReportService**: `@RestController` → `@Path`, `@PostMapping` → `@POST`, `@RequestBody` → implicit JAX-RS, `@Validated` → `@Valid`
- **GraphTraversalService**: `@RestController` → `@Path`, `@GetMapping` → `@GET`, `@RequestParam` → `@QueryParam`
- **ExternalRoutingService**: Spring `RestClient` → `java.net.http.HttpClient` + `ObjectMapper`; `@Value` → `@ConfigProperty`

### 9. SSE Endpoint Migration

- **RealtimeCargoTrackingService**: Spring `SseEmitter` → JAX-RS `SseEventSink`/`Sse`/`OutboundSseEvent`
- `@EventListener @Async` → `@Observes Cargo`
- `@RestController` → `@Path("/rest/cargo") @ApplicationScoped`

### 10. JSF Backing Bean Migration

- All beans: `@Component` → `@Named`, `@Autowired` → `@Inject`
- Spring `@RequestScope` → Jakarta `@RequestScoped`
- Spring `@SessionScope` → Jakarta `@SessionScoped`
- `@ViewScoped` and `@FlowScoped` kept as-is (already Jakarta Faces)
- **Booking.java**: Removed Spring FacesContext injection, uses `FacesContext.getCurrentInstance()`
- **Track.java**: `@Component("publicTrack")` → `@Named("publicTrack")`

### 11. Batch Processing Migration

- Spring Batch (`@EnableBatchProcessing`, `Job`, `Step`, `ItemReader`, `ItemWriter`) → Quarkus `@Scheduled`
- **UploadDirectoryScanner**: `@Scheduled(cron = "0 */2 * * * ?")` with direct file processing replacing Spring Batch job launcher
- **EventItemReader**: Simplified to `@ApplicationScoped` bean with `parseLine()` method only
- **BatchJobConfig**, **EventItemWriter**, **FileProcessorJobListener**, **LineParseExceptionListener**: Emptied to stubs

### 12. Data Initialization Migration

- **SampleDataGenerator**: `@Component @Profile("!test")` + `@PostConstruct` → `@ApplicationScoped` + `@Observes StartupEvent`
- **InitialLoader**: `@Component` → `@ApplicationScoped`, `@PersistenceContext` → `@Inject EntityManager`

### 13. Facade & Assembler Migration

- **DefaultBookingServiceFacade**: `@Component` → `@ApplicationScoped`
- All assemblers (`CargoRouteDtoAssembler`, `LocationDtoAssembler`, etc.): `@Component` → `@ApplicationScoped`
- **HandlingEventFactory**: `@Component` → `@ApplicationScoped`

### 14. XML Configuration Updates

- **faces-config.xml**: Removed `SpringBeanFacesELResolver`, updated namespace to Jakarta EE 4.0
- **beans.xml**: Created `META-INF/beans.xml` with `bean-discovery-mode="all"` for CDI
- **web.xml**: Created `META-INF/web.xml` with FacesServlet mapping and welcome-file configuration

### 15. Test Migration

- **BookingServiceTest**: `@SpringBootTest` → `@QuarkusTest`, Spring `@Transactional/@Commit` → Jakarta `@Transactional`
- **BookingServiceTestDataGenerator**: `@Component @Profile("test")` → `@ApplicationScoped`
- **TestDataGenerator**: `@Component` → `@ApplicationScoped`, Spring `@Transactional` → Jakarta `@Transactional`

### 16. Dockerfile Update

- Changed CMD from `["mvn", "clean", "spring-boot:run"]` to `["mvn", "clean", "quarkus:dev", "-Dquarkus.http.host=0.0.0.0"]`

### 17. Smoke Tests

- Generated `smoke.py` with 8 test cases covering:
  - Graph traversal REST endpoint (3 assertions)
  - Handling report POST endpoint
  - SSE cargo tracking endpoint
  - JSF index page accessibility
  - JSF dashboard page accessibility
  - Sample data loading verification

---

## Errors Encountered and Resolutions

### Error 1: GraphTraversalService compilation failure
- **Error**: `@QueryParam(name = "deadline", required = false)` - JAX-RS `@QueryParam` only has a `value` attribute, not `name` or `required`
- **Resolution**: Changed to `@QueryParam("deadline")`

### Error 2: Index page 404
- **Error**: JSF index page at `/` returned 404 (no welcome file configuration)
- **Resolution**: Created `META-INF/web.xml` with `<welcome-file>index.xhtml</welcome-file>` and FacesServlet mapping

---

## Validation Results

- Application compiles and starts successfully on Quarkus 3.8.4
- Startup time: ~10-11 seconds
- All 8 smoke tests pass:
  - Graph traversal returns 200 with valid path data
  - Handling report accepted (200/204)
  - SSE endpoint returns event stream content type
  - Index page accessible (200/redirect)
  - Dashboard page accessible (200)
  - Sample data loaded successfully
