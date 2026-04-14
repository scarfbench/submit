# Changelog

## [0.2.0] - 2026-03-14

### Migration: Jakarta EE (Open Liberty) to Quarkus 3.17.8

Complete migration of the Coffee Shop multi-module application from Jakarta EE 10 / MicroProfile 6.1 on Open Liberty to Quarkus 3.17.8.

### Changed

#### Build & Dependencies (pom.xml)
- **Parent POM**: Replaced Liberty Maven plugin and Jakarta EE / MicroProfile BOMs with Quarkus BOM (`io.quarkus.platform:quarkus-bom:3.17.8`) and `quarkus-maven-plugin`.
- **common module**: Replaced `jakarta.jakartaee-web-api` with `jakarta.validation-api` (managed by Quarkus BOM). Retained `jackson-databind`.
- **orders-service**: Removed WAR packaging (Quarkus default is JAR). Added `quarkus-rest-jackson`, `quarkus-hibernate-orm`, `quarkus-jdbc-postgresql`, `quarkus-jdbc-h2`, `quarkus-hibernate-validator`, `quarkus-messaging-kafka`, `quarkus-smallrye-health`, `quarkus-arc`. Removed Liberty-specific plugins.
- **barista-service / kitchen-service**: Added `quarkus-rest-jackson`, `quarkus-messaging-kafka`, `quarkus-smallrye-health`, `quarkus-arc`.

#### Java Source Code
- **JSON serialization**: Replaced Jakarta JSON-B (`jakarta.json.bind.Jsonb`, `JsonbBuilder`) and Jakarta JSON-P (`jakarta.json.Json`, `JsonObject`) with Jackson `ObjectMapper` across all services.
- **JPA injection**: Changed `@PersistenceContext(unitName = "ordersPU")` to `@Inject EntityManager` in `OrderRepository`. Added `em.flush()` after persist to ensure IDs are generated.
- **Bean Validation**: Converted `OrderRequest` from a Java record to a POJO class for Quarkus Hibernate Validator compatibility. Replaced `@NotBlank` with `@NotNull @Size(min=1)` to work with Quarkus build-time validation optimization.
- **Validation location**: Moved `@Valid` annotation from `OrderService.place()` to `OrdersResource.place()` for proper RESTEasy Reactive integration.
- **Kafka messaging**: Added try-catch around `Emitter.send()` calls in `OrdersPipeline` for graceful failure when Kafka is unavailable.

#### Configuration
- **Removed**: All Liberty-specific config files (`server.xml`, `server.env`, `web.xml`, `persistence.xml`, `microprofile-config.properties`).
- **Added**: `application.properties` for each service with Quarkus configuration.
- **Datasource**: H2 in-memory as default for all profiles; PostgreSQL available via `%pg` profile (`-Dquarkus.profile=pg`).
- **Kafka**: SmallRye Kafka connector with `lazy-client=true` and `retry=true` for graceful startup without a Kafka broker. All Kafka health checks disabled.
- **Health**: Exposed at `/health` via `quarkus.http.non-application-root-path=/`.
- **Jandex index**: Added `quarkus.index-dependency` for the `common` module so Quarkus discovers its validation annotations and domain types.

#### Static Resources
- Moved static web assets from `src/main/webapp/` to `src/main/resources/META-INF/resources/` (Quarkus convention).

#### Dockerfiles
- Updated root and per-service Dockerfiles to build Quarkus uber-jars (`mvn package -DskipTests -Dquarkus.package.jar.type=uber-jar`) and run with `java -jar`.

### Removed
- Duplicate `orders-service/web/RestApp.java` and `orders-service/web/OrdersResource.java` (kept `api/` package versions).
- All Open Liberty runtime dependencies and configuration.

### Smoke Test Results
All 11 checks passed:
- Health check (`/health`): 200 UP
- Create beverage order (Latte): 202 Accepted with order ID
- Get order by ID: 200 with correct customer, item, and PLACED status
- Create food order (Sandwich): 202 Accepted with order ID
- Validation (missing customer): 400 Bad Request
- Static content (`/coffeeshopTemplate.html`): 200 OK
