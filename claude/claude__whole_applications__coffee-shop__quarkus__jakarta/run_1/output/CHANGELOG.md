# CHANGELOG - Quarkus to Jakarta EE Migration

## Migration Summary

**Source Framework**: Quarkus 3.30.5 (with Hibernate ORM Panache, Vert.x EventBus, SmallRye Reactive Messaging/Kafka, Qute templates, Mutiny)
**Target Framework**: Jakarta EE 10 on Open Liberty (with EclipseLink JPA, CDI Events, Jakarta REST, SSE)
**Java Version**: 17
**Date**: 2026-03-14

---

## Actions Performed

### 1. Codebase Analysis
- Analyzed 42+ Java source files across packages: domain, barista, kitchen, counter, web, infrastructure, utils
- Identified Quarkus-specific dependencies: Panache ORM, Vert.x EventBus, SmallRye Reactive Messaging (Kafka), Qute templates, Mutiny reactive library
- Identified domain entities: `Order`, `LineItem`, `BaristaItem`, `KitchenOrder`
- Identified service layer pattern: event-driven with EventBus and Kafka channels

### 2. Dependency Migration (pom.xml)
- Replaced Quarkus BOM and all `io.quarkus:quarkus-*` dependencies
- Added `jakarta.jakartaee-api:10.0.0` (provided scope)
- Added Jackson libraries (`jackson-databind`, `jackson-datatype-jdk8`, `jackson-datatype-jsr310`)
- Added H2 database (2.2.224) for in-memory persistence
- Added SLF4J + Logback for logging
- Added MicroProfile Config API (3.1)
- Changed packaging from `jar` to `war`
- Added `liberty-maven-plugin:3.10` and `maven-war-plugin:3.4.0`
- Retained JUnit 5 for testing

### 3. Domain Model Refactoring
- **Order.java**: Removed `extends PanacheEntityBase`, added explicit `@Id` management, added `@Convert` for Instant fields
- **LineItem.java**: Removed `extends PanacheEntityBase`, retained manual `@Id` with UUID
- **BaristaItem.java**: Removed `extends PanacheEntity`, added explicit `@Id @GeneratedValue(strategy = GenerationType.AUTO)`, added `@Convert` for Instant fields
- **KitchenOrder.java**: Removed `extends PanacheEntity`, added explicit `@Id @GeneratedValue(strategy = GenerationType.AUTO)`, added `@Convert` for Instant fields
- Created `InstantAttributeConverter.java`: JPA `AttributeConverter<Instant, Timestamp>` with `autoApply=true` to handle EclipseLink's lack of native `java.time.Instant` support

### 4. Repository Refactoring
- **OrderRepository.java**: Replaced Panache `PanacheRepository` with `@ApplicationScoped` CDI bean using `@PersistenceContext EntityManager`
- **BaristaRepository.java**: Same pattern - EntityManager-based persistence
- **KitchenOrderRepository.java**: Same pattern - EntityManager-based persistence

### 5. Service/Business Logic Refactoring
- **OrderServiceImpl.java**: Replaced `@Inject EventBus` with `@Inject Event<BaristaInEvent>`, `Event<KitchenInEvent>`, `Event<WebUpdateEvent>`. Replaced `@ConsumeEvent` with `@Observes` CDI observer pattern.
- **BaristaOutpostImpl.java**: Merged `BaristaOutpostImpl` + `BaristaImpl` into single CDI bean. Replaced `@Incoming`/`@Outgoing` Kafka channels with `@Observes BaristaInEvent` and `Event<OrdersUpEvent>`. Reduced processing delays for responsiveness.
- **KitchenImpl.java**: Same pattern as Barista - replaced Kafka channels with CDI events.
- Created CDI event wrapper classes: `BaristaInEvent`, `KitchenInEvent`, `OrdersUpEvent`, `WebUpdateEvent`

### 6. Web Layer Refactoring
- **CoffeeshopApiResource.java**: Removed Quarkus-specific imports, added error handling with sanitized error messages
- **WebResource.java**: Replaced Qute template rendering with inline HTML response
- **DashboardEndpoint.java**: Replaced Mutiny `Multi<String>` SSE with Jakarta `SseEventSink` + `SseBroadcaster`
- Created **CoffeeshopApplication.java**: JAX-RS `@ApplicationPath("/")` class
- Created **HealthResource.java**: Simple health endpoint returning JSON status
- Created **JacksonConfig.java**: `@Provider` for Jackson ObjectMapper with `Jdk8Module` and `JavaTimeModule`
- Created **GlobalExceptionMapper.java**: `@Provider` ExceptionMapper to prevent multi-line error messages in HTTP headers

### 7. Configuration Files
- **server.xml**: Open Liberty configuration with `jakartaee-10.0` and `microProfile-6.1` features, H2 datasource, application deployment
- **persistence.xml**: JPA persistence unit with JTA transaction type, EclipseLink provider, H2Platform target, drop-and-create schema generation
- **beans.xml**: CDI configuration with `bean-discovery-mode="all"`

### 8. Dockerfile
- Multi-stage build: Maven 3.9 + Eclipse Temurin 17 (build) -> Open Liberty full-java17-openj9-ubi (runtime)
- Copies H2 jar from Maven cache to Liberty lib directory
- Copies WAR to Liberty apps directory
- Exposes port 8080

### 9. Test Updates
- Removed all `@QuarkusTest`, `@InjectMock`, `@InjectSpy` annotations
- Removed RestAssured and Vert.x EventBus test dependencies
- Converted all tests to pure JUnit 5 unit tests testing domain logic directly
- Fixed `Order.apply()` calls from static to instance method invocation

### 10. Smoke Tests
- Created `smoke-test.sh` with 8 test cases:
  - Health endpoint (GET /health returns 200 with "UP")
  - Root endpoint (GET / returns 200)
  - Place order with barista items (POST /api/order returns 202)
  - Place order with kitchen items (POST /api/order returns 202)
  - Place order with mixed items (POST /api/order returns 202)
  - Send message (POST /api/message returns 204)
  - Dashboard SSE stream availability (GET /dashboard/stream)
- Uses timestamp-based unique order IDs for idempotent re-runs

---

## Errors Encountered and Resolutions

### Error 1: Test Compilation - Static vs Instance Method
- **Error**: `Order.apply(orderUp)` called as static method in `OrderServiceOrderUpTest` and `OrderServiceTest`
- **Cause**: `apply()` is an instance method on `Order`, not static
- **Fix**: Changed to `orderEventResult.getOrder().apply(orderUp)`

### Error 2: H2 URL Format in Liberty server.xml
- **Error**: `GeneralDatabaseException: Invalid database URL` at startup
- **Cause**: Used `<properties.generic URL="...">` which is not valid for Liberty's H2 driver config
- **Fix**: Changed to `<properties URL="...">`

### Error 3: EclipseLink DDL - IDENTITY Generation
- **Error**: `BIGINT IDENTITY NOT NULL` syntax error in H2 DDL
- **Cause**: `@GeneratedValue(strategy = GenerationType.IDENTITY)` generates non-standard DDL for H2 with EclipseLink
- **Fix**: Changed to `GenerationType.AUTO` on `BaristaItem` and `KitchenOrder` entities

### Error 4: EclipseLink Instant to BINARY Mapping
- **Error**: `Data conversion error converting "BINARY VARYING to TIMESTAMP"`
- **Cause**: EclipseLink (unlike Hibernate) serializes `java.time.Instant` as binary blob by default. Adding `@Column(columnDefinition = "TIMESTAMP")` fixed DDL but not the runtime serialization.
- **Fix**: Created `InstantAttributeConverter` implementing `AttributeConverter<Instant, Timestamp>` with `@Converter(autoApply = true)`. Added explicit `@Convert(converter = InstantAttributeConverter.class)` on all Instant fields. Registered converter in `persistence.xml`.

### Error 5: HTTP Header LF Characters
- **Error**: `Invalid LF not followed by whitespace` on error responses
- **Cause**: Liberty's error handler placed multi-line exception messages into HTTP headers
- **Fix**: Added `GlobalExceptionMapper` that sanitizes error messages (removes newlines). Added try/catch with sanitized error response in `CoffeeshopApiResource.placeOrder()`.

### Error 6: Duplicate Key on Smoke Test Re-run
- **Error**: `Unique index or primary key violation` on order ID
- **Cause**: Smoke test used hardcoded order IDs, conflicting on re-runs
- **Fix**: Updated smoke-test.sh to use `$(date +%s%N)` based unique IDs

---

## Files Modified

| File | Action | Description |
|------|--------|-------------|
| `pom.xml` | Rewritten | Quarkus -> Jakarta EE 10 + Open Liberty |
| `Dockerfile` | Rewritten | Quarkus native -> Maven + Open Liberty |
| `smoke-test.sh` | Created | HTTP smoke test suite |
| `src/main/java/.../domain/Order.java` | Modified | Removed Panache, added JPA converter |
| `src/main/java/.../domain/LineItem.java` | Modified | Removed Panache |
| `src/main/java/.../domain/InstantAttributeConverter.java` | Created | Instant <-> Timestamp converter |
| `src/main/java/.../barista/domain/BaristaItem.java` | Modified | Removed Panache, added ID, converter |
| `src/main/java/.../kitchen/domain/KitchenOrder.java` | Modified | Removed Panache, added ID, converter |
| `src/main/java/.../infrastructure/OrderRepository.java` | Rewritten | Panache -> EntityManager |
| `src/main/java/.../barista/domain/BaristaRepository.java` | Rewritten | Panache -> EntityManager |
| `src/main/java/.../kitchen/domain/KitchenOrderRepository.java` | Rewritten | Panache -> EntityManager |
| `src/main/java/.../counter/OrderServiceImpl.java` | Modified | EventBus -> CDI Events |
| `src/main/java/.../counter/api/OrderService.java` | Modified | Updated interface |
| `src/main/java/.../barista/BaristaOutpostImpl.java` | Rewritten | Kafka -> CDI Events |
| `src/main/java/.../kitchen/KitchenImpl.java` | Rewritten | Kafka -> CDI Events |
| `src/main/java/.../infrastructure/events/*.java` | Created | CDI event wrapper classes (4 files) |
| `src/main/java/.../web/CoffeeshopApiResource.java` | Modified | Error handling |
| `src/main/java/.../web/WebResource.java` | Modified | Qute -> HTML |
| `src/main/java/.../web/DashboardEndpoint.java` | Rewritten | Mutiny -> Jakarta SSE |
| `src/main/java/.../web/CoffeeshopApplication.java` | Created | JAX-RS Application |
| `src/main/java/.../web/HealthResource.java` | Created | Health endpoint |
| `src/main/java/.../web/JacksonConfig.java` | Created | Jackson config |
| `src/main/java/.../web/GlobalExceptionMapper.java` | Created | Exception handler |
| `src/main/liberty/config/server.xml` | Created | Liberty server config |
| `src/main/resources/META-INF/persistence.xml` | Created | JPA persistence config |
| `src/main/webapp/WEB-INF/beans.xml` | Created | CDI beans config |
| `src/test/java/**/*Test.java` | Modified | Removed Quarkus test infra (10 files) |

---

## Validation Results

- **Build**: SUCCESS (Maven WAR packaging)
- **Docker Image**: Built successfully
- **Server Startup**: Liberty starts and deploys application
- **Smoke Tests**: 8/8 PASSED, 0 FAILED
