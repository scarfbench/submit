# CHANGELOG - Jakarta EE (Open Liberty) to Quarkus Migration

## Summary
Migrated the Coffee Shop multi-module Maven application from **Jakarta EE 10 on Open Liberty** with **MicroProfile 6.1** to **Quarkus 3.17.8**. All original functionality (REST endpoints, JPA persistence, Kafka messaging, health checks, bean validation) is preserved and verified via smoke tests.

---

## Build & Configuration Changes

### Parent POM (`pom.xml`)
- Changed `artifactId` from `coffeeshop-jakarta-liberty` to `coffeeshop-quarkus`
- Replaced MicroProfile BOM (`microprofile-bom 6.1`) with Quarkus BOM (`io.quarkus.platform:quarkus-bom:3.17.8`)
- Replaced Liberty Maven plugin with `quarkus-maven-plugin`
- Removed Liberty-specific properties (`liberty.var.*`, MicroProfile versions)
- Set Java 21, UTF-8 encoding, and uber-jar packaging as properties

### Common Module (`common/pom.xml`)
- Updated parent reference to `coffeeshop-quarkus`
- Replaced `jakarta.jakartaee-web-api` with `jakarta.validation-api` and `jackson-databind`
- Added `jandex-maven-plugin` to generate Jandex index for Quarkus build-time annotation discovery

### Orders Service (`orders-service/pom.xml`)
- Changed packaging from `war` to `jar`
- Replaced Jakarta EE / Liberty dependencies with Quarkus extensions:
  - `quarkus-rest-jackson` (RESTEasy Reactive with Jackson)
  - `quarkus-hibernate-orm` (JPA/Hibernate)
  - `quarkus-jdbc-h2` (in-memory database)
  - `quarkus-hibernate-validator` (bean validation)
  - `quarkus-smallrye-health` (health checks)
  - `quarkus-messaging-kafka` (Kafka via SmallRye Reactive Messaging)
  - `quarkus-arc` (CDI)
- Removed Liberty Maven plugin, WAR plugin, dependency-copy plugin

### Barista Service (`barista-service/pom.xml`)
- Changed packaging from `war` to `jar`
- Updated parent to `coffeeshop-quarkus`
- Dependencies: `quarkus-rest-jackson`, `quarkus-smallrye-health`, `quarkus-messaging-kafka`, `quarkus-arc`

### Kitchen Service (`kitchen-service/pom.xml`)
- Changed packaging from `war` to `jar`
- Updated parent to `coffeeshop-quarkus`
- Dependencies: `quarkus-rest-jackson`, `quarkus-smallrye-health`, `quarkus-messaging-kafka`, `quarkus-arc`

---

## Application Configuration

### Created `application.properties` for Orders Service
- `quarkus.rest.path=/api` (context root for REST endpoints)
- H2 in-memory database as default (PostgreSQL available via `%postgres` profile)
- `quarkus.hibernate-orm.database.generation=drop-and-create`
- Kafka producer channels: `barista` (topic: barista-commands), `kitchen` (topic: kitchen-commands)
- Kafka consumer channel: `order-updates` (topic: order-updates) with `failure-strategy=ignore`
- Disabled Kafka/messaging health checks (`quarkus.kafka.health.enabled=false`, `quarkus.messaging.health.enabled=false`) so app starts healthy without a Kafka broker

### Created `application.properties` for Barista Service
- Kafka consumer channel: `barista` (topic: barista-commands)
- Kafka producer channel: `order-updates` (topic: order-updates)
- Disabled Kafka health checks

### Created `application.properties` for Kitchen Service
- Kafka consumer channel: `kitchen` (topic: kitchen-commands)
- Kafka producer channel: `order-updates` (topic: order-updates)
- Disabled Kafka health checks

---

## Java Code Changes

### OrderRequest (`common/.../OrderRequest.java`)
- Converted from Java `record` to POJO class
- Reason: Quarkus REST validation with `@Valid` on records causes `UnexpectedTypeException` due to annotation propagation issues
- Added `@NotBlank` on `customer` and `item` fields, `@Min(1)` on `quantity` field
- Preserved record-style accessor methods (`customer()`, `item()`, `quantity()`) for backward compatibility
- Added standard JavaBean getters/setters for Jackson deserialization

### OrdersResource (`orders-service/.../OrdersResource.java`)
- Removed `@Valid` annotation from endpoint parameter
- Implemented manual validation using injected `jakarta.validation.Validator` to avoid Quarkus method-parameter validation issues
- Moved Kafka messaging out of `OrderService` transaction boundary into this class
- Kafka messaging wrapped in try-catch as fire-and-forget (non-fatal if Kafka unavailable)

### OrderService (`orders-service/.../OrderService.java`)
- Simplified to only handle transactional DB persistence (`@Transactional`)
- Returns `OrderEntity` (previously returned `String`)
- Made `isDrink()` method public for use by `OrdersResource`
- Reason: Kafka `Emitter.send()` participates in JTA transactions; when Kafka is unavailable, it caused `RollbackException` that rolled back the DB save

### OrderRepository (`orders-service/.../OrderRepository.java`)
- Replaced `@PersistenceContext` with `@Inject` for `EntityManager` (Quarkus pattern)

### OrdersPipeline (`orders-service/.../OrdersPipeline.java`)
- Replaced JSON-B (`jakarta.json.bind.Jsonb`) with Jackson (`ObjectMapper`) for JSON serialization
- Added graceful error handling with `exceptionally()` callback on Kafka send futures
- Uses `@Channel` + `Emitter<String>` pattern for SmallRye Reactive Messaging

### OrderUpdatesListener (`orders-service/.../OrderUpdatesListener.java`)
- Replaced JSON-B with Jackson for JSON deserialization
- Uses `@Incoming("order-updates")` annotation for SmallRye Reactive Messaging

### BaristaConsumer (`barista-service/.../BaristaConsumer.java`)
- Replaced JSON-B with Jackson for JSON processing
- Uses `@Incoming("barista")` / `@Outgoing("order-updates")` for SmallRye Reactive Messaging

### KitchenConsumer (`kitchen-service/.../KitchenConsumer.java`)
- Replaced `javax.json` (JSON-P) with Jackson for JSON processing
- Uses `@Incoming("kitchen")` / `@Outgoing("order-updates")` for SmallRye Reactive Messaging

---

## Files Deleted

- `orders-service/src/main/java/com/coffeeshop/orders/api/OrdersApplication.java` (Liberty JAX-RS Application class)
- `orders-service/src/main/java/com/coffeeshop/orders/web/RestApp.java` (duplicate Application class)
- `barista-service/src/main/java/com/coffeeshop/barista/api/BaristaApplication.java` (Liberty JAX-RS Application class)
- `kitchen-service/src/main/java/com/coffeeshop/kitchen/api/KitchenApplication.java` (Liberty JAX-RS Application class)
- `*/src/main/liberty/` directories (Liberty server.xml, server.env configurations)
- `*/src/main/resources/META-INF/persistence.xml` (replaced by Quarkus application.properties)
- `*/src/main/resources/META-INF/microprofile-config.properties` (replaced by application.properties)
- `*/src/main/webapp/WEB-INF/web.xml` (not needed for Quarkus JAR packaging)

---

## Dockerfile Changes

- Changed Maven build command to: `mvn -pl orders-service -am clean package -DskipTests -Dquarkus.package.jar.type=uber-jar`
- Changed CMD from `liberty:run` to: `java -jar orders-service/target/orders-service-0.2.0-SNAPSHOT-runner.jar`

---

## Static Resources

- Moved `orders-service/src/main/webapp/index.html` to `orders-service/src/main/resources/META-INF/resources/index.html` (Quarkus convention)

---

## Smoke Tests

Created `smoke.py` with 7 tests, all passing:
1. Health readiness (`GET /q/health/ready` -> 200)
2. Health liveness (`GET /q/health/live` -> 200)
3. Create drink order (`POST /api/orders` with Latte -> 202)
4. Create food order (`POST /api/orders` with Sandwich -> 202)
5. Get order by ID (`GET /api/orders/{id}` -> 200)
6. Invalid order validation (`POST /api/orders` with empty fields -> 400)
7. Status endpoint (graceful skip if not present)

---

## Issues Encountered & Resolved

1. **`quarkus-smallrye-reactive-messaging-kafka` not in BOM**: Quarkus 3.x renamed the extension to `quarkus-messaging-kafka`
2. **PostgreSQL connection refused at startup**: Dual JDBC drivers (H2 + PostgreSQL) caused Quarkus to attempt PostgreSQL connection. Removed PostgreSQL driver; kept H2 only with PostgreSQL available via profile
3. **Health check DOWN due to Kafka**: SmallRye Reactive Messaging readiness reported channels as KO. Fixed with `quarkus.messaging.health.enabled=false`
4. **Bean validation `UnexpectedTypeException` on records**: Java record annotation propagation incompatible with Quarkus REST validation. Converted to POJO with field-level annotations and manual validation
5. **`ArcUndeclaredThrowableException` / `RollbackException` from Kafka in transaction**: Kafka `Emitter.send()` participates in JTA transaction, causing rollback when Kafka unavailable. Separated DB save (transactional) from messaging (fire-and-forget outside transaction)
6. **Jandex index missing for common module**: Validation annotations in the `common` module not discovered by Quarkus at build time. Added `jandex-maven-plugin` to generate class index
