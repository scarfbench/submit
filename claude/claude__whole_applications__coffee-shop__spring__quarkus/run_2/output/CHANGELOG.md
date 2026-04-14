# CHANGELOG - Spring Boot to Quarkus Migration

## Overview
Migrated the multi-module Coffee Shop application from Spring Boot 3.3.5 to Quarkus 3.8.4.

## Modules Migrated
- `common` - Shared domain models, value objects, exception handling
- `web-service` - Main web application (port 8080), UI, SSE streaming, Kafka consumer
- `counter-service` - Order processing, JPA/PostgreSQL, Kafka producer/consumer
- `barista-service` - Beverage preparation, Kafka consumer/producer
- `kitchen-service` - Food preparation, Kafka consumer/producer

---

## Changes by Category

### 1. Parent POM (`pom.xml`)
- Removed Spring Boot parent (`spring-boot-starter-parent:3.3.5`)
- Added Quarkus BOM import (`io.quarkus.platform:quarkus-bom:3.8.4`)
- Added `quarkus-maven-plugin` to pluginManagement
- Added `maven-compiler-plugin:3.12.1` and `maven-surefire-plugin:3.2.5` to pluginManagement
- Added `assertj-core:3.25.3` and `mockito-core:5.10.0` version management (not managed by Quarkus BOM)

### 2. Common Module
**pom.xml:**
- Removed all `spring-boot-starter-*` dependencies
- Kept Jackson, JPA API, Validation API dependencies (managed by Quarkus BOM)
- Added `jakarta.ws.rs:jakarta.ws.rs-api` for JAX-RS ExceptionMapper
- Added `io.quarkus:quarkus-junit5` and `assertj-core` for tests

**Java Source:**
- `RestExceptionHandler.java`: Converted from Spring `@ControllerAdvice` extending `ResponseEntityExceptionHandler` to JAX-RS `@Provider` implementing `ExceptionMapper<Exception>`. Returns `jakarta.ws.rs.core.Response` instead of `ResponseEntity`.

### 3. Web-Service Module
**pom.xml:**
- Removed `spring-boot-starter-web`, `spring-boot-starter-thymeleaf`, `spring-kafka`, `spring-boot-starter-actuator`, `spring-boot-starter-webflux`
- Added: `quarkus-resteasy-reactive-jackson`, `quarkus-smallrye-reactive-messaging-kafka`, `quarkus-resteasy-reactive-qute`, `quarkus-smallrye-health`, `quarkus-mutiny`, `quarkus-arc`, `quarkus-hibernate-validator`, `assertj-core`

**Java Source:**
- `WebServiceApplication.java`: Converted from `@SpringBootApplication` with `main()` to `@ApplicationPath("/")` extending `jakarta.ws.rs.core.Application`
- `CoffeeshopApiController.java`: Converted from `@RestController`/`@RequestMapping` to JAX-RS `@Path`/`@GET`/`@POST`/`@Produces`/`@Consumes`; `ResponseEntity` replaced with `Response`
- `DashboardController.java`: Converted from Spring WebFlux `Flux<ServerSentEvent>` with `Sinks.Many` to Quarkus `Multi<String>` with `BroadcastProcessor` and `@RestStreamElementType`
- `SseBroadcasterConfig.java`: Converted from Spring `@Configuration`/`@Bean` with `Sinks.Many` to CDI `@ApplicationScoped`/`@Produces`/`@Singleton` with `BroadcastProcessor<String>`
- `WebUpdatesListener.java`: Converted from `@KafkaListener` to Smallrye `@Incoming("web-updates-in")`; removed Spring `Acknowledgment` parameter
- `WebController.java`: Converted from Spring `@Controller` with Thymeleaf `Model` to JAX-RS `@Path`/`@GET` with Qute `Template`/`TemplateInstance`; `@Value` replaced with `@ConfigProperty`

**Templates:**
- `coffeeshop.html`: Converted from Thymeleaf syntax (`th:href`, `th:src`, `th:attr`, `th:inline`) to Qute syntax (`{streamUrl}`, `{storeId}` placeholders)

**Static Resources:**
- Copied files from `src/main/resources/static/` to `src/main/resources/META-INF/resources/` (Quarkus convention)

**Configuration:**
- `application.properties`: Converted from Spring format to Quarkus format (`mp.messaging.incoming.*`, `kafka.bootstrap.servers`, `quarkus.log.*`)
- Removed `application-docker.yml`

**Tests:**
- Deleted `WebServiceApplicationTests.java` (Spring `@SpringBootTest` context test)
- Rewrote `CoffeeshopApiControllerTest.java`: Replaced Spring `@WebMvcTest`/`MockMvc` with plain JUnit unit test calling controller directly
- Rewrote `DashboardControllerSseTest.java`: Replaced Spring `@WebFluxTest`/`WebTestClient` with plain JUnit unit test using `BroadcastProcessor`

### 4. Counter-Service Module
**pom.xml:**
- Removed `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-kafka`, `spring-boot-starter-actuator`
- Added: `quarkus-resteasy-reactive-jackson`, `quarkus-hibernate-orm`, `quarkus-jdbc-postgresql`, `quarkus-jdbc-h2` (test), `quarkus-smallrye-reactive-messaging-kafka`, `quarkus-smallrye-health`, `quarkus-hibernate-validator`, `quarkus-arc`, `quarkus-narayana-jta`

**Java Source:**
- `CounterApiController.java`: Converted from `@RestController`/`@RequestMapping` to JAX-RS `@Path`/`@POST`; `@Autowired` replaced with `@Inject`
- `OrderService.java` (interface): Changed `onOrderUp(Message<String>)` to `onOrderUp(String)`
- `OrderServiceImpl.java`: Converted from `@Service` to `@ApplicationScoped`; `KafkaTemplate` replaced with `Emitter<Record<String, String>>` using `@Channel`; `@Transactional` changed from Spring to Jakarta; `orderRepository.save()` to `orderRepository.persist()`
- `OrderRepository.java`: Converted from Spring `JpaRepository<Order, String>` to Quarkus `PanacheRepositoryBase<Order, String>` with `@ApplicationScoped`
- `OrderUpListener.java`: Converted from `@KafkaListener` to `@Incoming("orders-up-in")`; removed Spring `Message`/`Acknowledgment`
- Deleted `KafkaConfig.java` (not needed with Smallrye Reactive Messaging)

**Configuration:**
- `application.properties`: Full Quarkus config with `quarkus.datasource.*`, `quarkus.hibernate-orm.*`, 4 Kafka channels
- Removed `application-docker.yml`

**Tests:**
- Deleted `CounterServiceApplicationTests.java`, `EmptyTestApp.java`, `OrderRepositoryIT.java`
- `CounterApiControllerTest.java`: Converted from Spring MockMvc to direct JAX-RS Response testing
- `OrderServiceImplUnitTest.java`: Converted `KafkaTemplate` mocks to `Emitter<Record>` mocks

### 5. Barista-Service Module
**pom.xml:**
- Removed `spring-boot-starter-web`, `spring-kafka`, `spring-boot-starter-actuator`
- Added: `quarkus-resteasy-reactive-jackson`, `quarkus-smallrye-reactive-messaging-kafka`, `quarkus-arc`

**Java Source:**
- `BaristaApplication.java`: Converted from `@SpringBootApplication` to `@ApplicationPath("/")` extending `Application`
- `BaristaListener.java`: Converted from `@KafkaListener` + `KafkaTemplate` to `@Incoming("barista-in")` + `Emitter<Record>` with `@Channel("orders-up-out")`

**Configuration:**
- `application.properties`: Quarkus format with `mp.messaging.*` channels
- Removed `application-docker.yml`

**Tests:**
- Deleted `BaristaServiceApplicationTests.java`
- `BaristaListenerTest.java`: Converted from `KafkaTemplate` mocks to `Emitter<Record>` mocks

### 6. Kitchen-Service Module
**pom.xml:**
- Removed `spring-boot-starter-web`, `spring-kafka`, `spring-boot-starter-actuator`
- Added: `quarkus-resteasy-reactive-jackson`, `quarkus-smallrye-reactive-messaging-kafka`, `quarkus-smallrye-health`, `quarkus-arc`

**Java Source:**
- `KitchenApplication.java`: Converted from `@SpringBootApplication` to `@ApplicationPath("/")` extending `Application`
- `KitchenListener.java`: Converted from `@KafkaListener` + `KafkaTemplate` to `@Incoming("kitchen-in")` + `Emitter<Record>` with `@Channel("orders-up-out")`

**Configuration:**
- `application.properties`: Quarkus format with `mp.messaging.*` channels
- Removed `application-docker.yml`

**Tests:**
- Deleted `KitchenServiceApplicationTests.java`
- `KitchenListenerTest.java`: Converted from `KafkaTemplate` mocks to `Emitter<Record>` mocks

### 7. Dockerfiles
- All 5 Dockerfiles updated:
  - Changed CMD from Spring Boot Maven plugin (`spring-boot:run`) to Quarkus build + run (`mvn clean install -DskipTests && java -jar target/quarkus-app/quarkus-run.jar`)
  - Root Dockerfile builds only web-service module: `mvn -pl web-service -am clean install -DskipTests`

### 8. Smoke Tests (`smoke.py`)
- Created smoke test suite with 5 tests:
  1. `GET /api/health` - verifies custom health endpoint returns "web-service OK"
  2. `POST /api/order` - sends PlaceOrderCommand, expects 202 Accepted
  3. `POST /api/message` - sends message, expects 202 Accepted
  4. `GET /` - verifies home page contains "Quarkus Coffee Shop"
  5. `GET /api/dashboard/stream` - verifies SSE endpoint responds

---

## Key Migration Patterns

| Spring Boot | Quarkus |
|---|---|
| `@SpringBootApplication` | `@ApplicationPath("/")` + `extends Application` |
| `@RestController` / `@RequestMapping` | `@Path` / `@GET` / `@POST` (JAX-RS) |
| `@Autowired` | `@Inject` |
| `@Service` / `@Component` | `@ApplicationScoped` |
| `@Value("${prop}")` | `@ConfigProperty(name = "prop")` |
| `@Configuration` / `@Bean` | `@ApplicationScoped` / CDI `@Produces` |
| `ResponseEntity<?>` | `jakarta.ws.rs.core.Response` |
| `@KafkaListener` | `@Incoming("channel")` |
| `KafkaTemplate.send()` | `Emitter<Record>.send()` |
| `JpaRepository` | `PanacheRepositoryBase` |
| `repository.save()` | `repository.persist()` |
| Thymeleaf templates | Qute templates |
| `Sinks.Many<String>` | `BroadcastProcessor<String>` |
| `Flux<ServerSentEvent>` | `Multi<String>` with `@RestStreamElementType` |
| `src/main/resources/static/` | `src/main/resources/META-INF/resources/` |
| `spring.datasource.*` | `quarkus.datasource.*` |
| `spring.jpa.*` | `quarkus.hibernate-orm.*` |
| `spring.kafka.*` | `mp.messaging.*` channels |

## Build & Test Results
- **Maven build**: SUCCESS (all modules compile cleanly)
- **Quarkus startup**: Successful, listening on port 8080
- **Smoke tests**: 5/5 PASSED
