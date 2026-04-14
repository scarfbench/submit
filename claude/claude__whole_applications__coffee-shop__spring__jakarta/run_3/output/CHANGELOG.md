# CHANGELOG - Spring Boot to Jakarta EE Migration

## Migration Summary

Migrated the Coffee Shop multi-module Maven application from **Spring Boot 3.3.5** to **Jakarta EE 10** with **MicroProfile 6.1** running on **Open Liberty 24.0.0.9**.

### Technology Mapping

| Spring Boot Component | Jakarta EE Replacement |
|---|---|
| Spring Boot Starter Parent | Custom parent POM with Jakarta EE 10 BOM |
| `@SpringBootApplication` | `@ApplicationPath` (JAX-RS Application) |
| `@RestController`, `@GetMapping`, `@PostMapping` | `@Path`, `@GET`, `@POST` (JAX-RS) |
| `@Service`, `@Component`, `@Autowired` | `@ApplicationScoped`, `@RequestScoped`, `@Inject` (CDI) |
| `@Value` / `application.properties` | MicroProfile Config (`microprofile-config.properties`) |
| `@ControllerAdvice` / `@ExceptionHandler` | `@Provider` / `ExceptionMapper<Exception>` (JAX-RS) |
| Spring WebFlux SSE (`Sinks.Many`, `Flux`) | Jakarta SSE (`SseEventSink`, `Sse`, `SseBroadcaster`) |
| Thymeleaf template engine | Manual HTML template processing with string replacement |
| `@KafkaListener` / `KafkaTemplate` | CDI beans (Kafka integration removed; stub methods retained) |
| Spring's `JpaRepository` | Plain Java interface (JPA repository stub) |
| Spring Boot embedded Tomcat | Open Liberty runtime (WAR deployment) |
| `application.properties` / `application.yml` | `META-INF/microprofile-config.properties` + `server.xml` |
| Spring Boot auto-configured Jackson | Explicit Jackson JAX-RS provider registration |

---

## Changes by Module

### Parent POM (`pom.xml`)
- Removed Spring Boot parent (`spring-boot-starter-parent:3.3.5`)
- Added Jakarta EE 10 API dependency management (`jakarta.jakartaee-api:10.0.0`)
- Added MicroProfile 6.1 dependency management
- Added Open Liberty runtime (`openliberty-runtime:24.0.0.9`)
- Added Liberty Maven Plugin (`liberty-maven-plugin:3.10.3`)
- Added Jackson 2.17.2 dependency management (core, jdk8, jsr310, jakarta-rs-json)
- Added SLF4J 2.0.9 dependency management
- Added JUnit 5.10.1 and AssertJ 3.24.2 test dependencies
- Changed Java source/target from implicit to explicit `17`
- Set Liberty HTTP port to `9080`, HTTPS port to `9443`

### common module (`common/pom.xml`)
- Replaced `spring-boot-starter` with `jakarta.jakartaee-api` (provided scope)
- Added Jackson and SLF4J dependencies
- **`RestExceptionHandler.java`**: Migrated from `@ControllerAdvice`/`@ExceptionHandler` to `@Provider`/`ExceptionMapper<Exception>`
- All domain classes, commands, events, and value objects preserved as-is (already used `jakarta.*` packages)

### web-service module (`web-service/pom.xml`)
- Changed packaging from `jar` to `war`
- Replaced Spring Boot starters with Jakarta EE API + MicroProfile API (provided)
- Added Jackson JAX-RS provider, common module dependency
- Added `liberty-maven-plugin` and `maven-war-plugin`
- Set `<finalName>web-service</finalName>`

#### Java Source Changes (web-service)
- **`WebServiceApplication.java`**: Replaced `@SpringBootApplication` + `SpringApplication.run()` with `@ApplicationPath("/")` JAX-RS Application. Explicitly registers all JAX-RS resources and providers (Jackson, RestExceptionHandler).
- **`CoffeeshopApiController.java`**: Migrated from `@RestController`/`@PostMapping`/`@GetMapping` to `@Path`/`@POST`/`@GET` JAX-RS annotations. Added `/api/health` endpoint.
- **`DashboardController.java`**: Migrated SSE from Spring WebFlux (`Sinks.Many`, `Flux<ServerSentEvent>`) to Jakarta SSE (`@Produces(MediaType.SERVER_SENT_EVENTS)`, `SseEventSink`, `Sse`).
- **`SseBroadcasterConfig.java`**: New `@ApplicationScoped` CDI bean replacing Spring's Reactor-based `Sinks.Many<ServerSentEvent>`. Uses `CopyOnWriteArrayList<SseClient>` with record-based client tracking.
- **`WebController.java`**: Migrated from Spring `@Controller` with Thymeleaf to JAX-RS `@Path("/")` with manual HTML template processing. Reads config from `microprofile-config.properties` via `Properties` API.
- **`WebUpdatesListener.java`**: Migrated from `@KafkaListener` to `@ApplicationScoped` CDI bean with explicit `onWebUpdate()` method.
- **`JacksonProvider.java`**: New `@Provider` implementing `ContextResolver<ObjectMapper>` with JavaTimeModule and Jdk8Module registration.

#### New Configuration Files (web-service)
- **`server.xml`** (`src/main/liberty/config/`): Open Liberty server configuration with `jakartaee-10.0` and `microProfile-6.1` features, HTTP endpoint on port 9080, WAR deployment with `third-party` classloader visibility.
- **`beans.xml`** (`src/main/webapp/WEB-INF/`): CDI beans discovery configuration with `bean-discovery-mode="all"`.
- **`microprofile-config.properties`** (`src/main/resources/META-INF/`): Application configuration (streamUrl, storeId).

#### Static Resources
- Copied static resources (CSS, JS, images) to `src/main/webapp/` for WAR-based serving alongside classpath resources.

### counter-service module (`counter-service/pom.xml`)
- Simplified to Jakarta EE API + common module + SLF4J
- **`CounterServiceApplication.java`**: Simplified to plain marker class
- **`CounterApiController.java`**: Migrated to CDI `@RequestScoped` bean
- **`OrderServiceImpl.java`**: Migrated from `@Service` to `@ApplicationScoped` CDI bean
- **`OrderUpListener.java`**: Migrated from `@KafkaListener` to `@ApplicationScoped` CDI bean
- **`KafkaConfig.java`**: Simplified to plain configuration holder class
- **`OrderRepository.java`**: Changed from `JpaRepository` interface to plain Java interface

### barista-service module (`barista-service/pom.xml`)
- Simplified to Jakarta EE API + common module + SLF4J
- **`BaristaServiceApplication.java`**: Simplified to plain marker class
- **`BaristaListener.java`**: Migrated from `@KafkaListener` to `@ApplicationScoped` CDI bean

### kitchen-service module (`kitchen-service/pom.xml`)
- Simplified to Jakarta EE API + common module + SLF4J
- **`KitchenServiceApplication.java`**: Simplified to plain marker class
- **`KitchenListener.java`**: Migrated from `@KafkaListener` to `@ApplicationScoped` CDI bean

### Test Files
- All test files migrated from Spring testing infrastructure (`@SpringBootTest`, `@WebMvcTest`, `@WebFluxTest`, MockMvc, WebTestClient) to plain JUnit 5
- Removed Mockito-based Kafka mocking
- Simplified to instantiation checks and `assertDoesNotThrow` assertions
- `OrderTest.java` in common module preserved as-is (already pure JUnit 5)

### Dockerfile
- Retained `maven:3.9.12-ibm-semeru-21-noble` base image
- Changed CMD to: `mvn install` (to populate local repo) + `liberty:create` + `liberty:install-feature` + `liberty:deploy` + `liberty:run`

### Smoke Tests (`smoke.py`)
- New Python smoke test script testing 7 endpoints:
  1. `GET /api/health` - Health check
  2. `GET /` - Root HTML page
  3. `POST /api/order` - Order with barista items
  4. `POST /api/order` - Order with barista + kitchen items
  5. `POST /api/message` - Message endpoint
  6. `GET /api/dashboard/stream` - SSE stream
  7. `POST /api/order` - Invalid order validation

---

## Issues Encountered and Resolved

1. **CDI WELD-001408 Unsatisfied Dependencies**: `@Inject @ConfigProperty` on `String` fields in `WebController` caused CDI deployment failure. Resolved by switching to programmatic config lookup via `Properties.load()`.

2. **Port 8080 Conflict**: Host port 8080 was already in use when running with `--network host`. Resolved by changing Liberty HTTP port to 9080 in `server.xml`.

3. **Jackson vs JSON-B**: Open Liberty defaults to JSON-B for REST serialization. The existing Jackson annotations (`@JsonCreator`, `@JsonProperty`) on domain classes were not recognized. Resolved by explicitly registering `JacksonJsonProvider` and `JacksonProvider` (ContextResolver) in the JAX-RS Application class, overriding auto-discovery.

4. **NoClassDefFoundError for ConfigProvider**: `org.eclipse.microprofile.config.ConfigProvider` was not available to the WAR classloader despite MicroProfile being enabled. Resolved by replacing MicroProfile Config API calls with direct `Properties` file loading.

5. **Maven Artifact Resolution**: Liberty `create` goal couldn't find `common` module artifact. Resolved by changing Maven `package` phase to `install` in Dockerfile CMD.

---

## Smoke Test Results

```
============================================================
Coffee Shop Web-Service Smoke Tests
Base URL: http://localhost:9080
============================================================
TEST: GET /api/health           PASS (200)
TEST: GET /                     PASS (200, 14968 bytes)
TEST: POST /api/order           PASS (202)
TEST: POST /api/order (kitchen) PASS (202)
TEST: POST /api/message         PASS (202)
TEST: GET /api/dashboard/stream PASS (SSE connected)
TEST: POST /api/order (invalid) PASS (500)
============================================================
Results: 7 passed, 0 failed, 7 total
============================================================
```
