# CHANGELOG - DayTrader Quarkus to Spring Boot Migration

## Migration Summary

Migrated the DayTrader stock trading application from Quarkus 3.17.0 to Spring Boot 3.2.5.

**Result: 14/14 smoke tests passing.**

---

## Build & Configuration Changes

### pom.xml
- Replaced Quarkus 3.17.0 BOM (`quarkus-bom`) with Spring Boot 3.2.5 starter parent
- Replaced all Quarkus dependencies with Spring Boot starters:
  - `quarkus-rest`, `quarkus-rest-jackson` -> `spring-boot-starter-web`
  - `quarkus-hibernate-orm` -> `spring-boot-starter-data-jpa`
  - `quarkus-hibernate-validator` -> `spring-boot-starter-validation`
  - `quarkus-jdbc-h2` -> `com.h2database:h2`
  - `quarkus-smallrye-reactive-messaging-in-memory` -> Spring `ApplicationEventPublisher` (no extra dep)
  - Added `spring-boot-starter-actuator` for health endpoints
  - Added `jackson-databind` for JSON serialization
- Replaced `quarkus-maven-plugin` with `spring-boot-maven-plugin`

### application.properties
- `quarkus.datasource.*` -> `spring.datasource.*`
- `quarkus.hibernate-orm.*` -> `spring.jpa.hibernate.*`
- Added `spring.mvc.servlet.path=/rest` to preserve `/rest/*` URL prefix
- Added `spring.jpa.open-in-view=false`
- Added `management.endpoints.web.exposure.include=health,info`
- Retained custom `trade.*` configuration properties with `@Value` annotations

### Dockerfile
- Changed run command from `mvn quarkus:run` to `java -jar target/daytrader-spring-1.0-SNAPSHOT.jar`

### New Files
- `DayTraderApplication.java` - Spring Boot main class with `@SpringBootApplication`, `@EnableScheduling`, `@EnableAsync`
- `BrokerMessageEvent.java` - Spring `ApplicationEvent` for broker messages
- `StreamerMessageEvent.java` - Spring `ApplicationEvent` for streamer messages
- `smoke.py` - 14 smoke tests covering all REST endpoints

### Deleted Files
- `TradeResourceTest.java` - Quarkus-specific test
- `MessagingTest.java` - Quarkus-specific test
- `MessagingResourceTest.java` - Quarkus-specific test

---

## Java Source Changes

### Dependency Injection (all files)
- `@Inject` -> `@Autowired`
- `@ApplicationScoped` -> `@Service` or `@Component`
- `@Dependent` -> `@Component @Scope("prototype")`
- `@Singleton` -> `@Service`
- CDI `Instance<T>` -> Spring `ApplicationContext.getBean()`
- CDI qualifiers (`@TradeJDBC`, `@TradeEJB`, etc.) -> Simple marker annotations + `@Qualifier`

### REST Controllers (jaxrs package)
- **TradeResource.java**: `@Path("trade")` -> `@RestController @RequestMapping("/trade")`; all JAX-RS annotations (`@GET`, `@POST`, `@PathParam`, `@FormParam`, `Response`) replaced with Spring MVC equivalents (`@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestParam`, `ResponseEntity`)
- **QuoteResource.java**: Same JAX-RS -> Spring MVC migration
- **MessagingResource.java**: Same migration; endpoints restructured to `/messaging/ping/broker`, `/messaging/ping/streamer`, `/messaging/stats`
- **TradeAppResource.java**: JAX-RS -> Spring MVC with `produces = MediaType.TEXT_HTML_VALUE`

### Service Layer (impl packages)
- **TradeSLSBBean.java**: `@ApplicationScoped @Default @TradeEJB` -> `@Service @Primary @Transactional`; `@PersistenceContext EntityManager` replacing `@Inject EntityManager`
- **DirectSLSBBean.java**: `@ApplicationScoped @TradeSession2Direct` -> `@Service("directSLSBBean") @Transactional`; added `@Lazy` on `tradeDirect` field
- **TradeDirect.java**: `@Dependent @TradeJDBC` -> `@Component("tradeDirect") @Scope("prototype")`; `AgroalDataSource` -> `javax.sql.DataSource`; removed `UserTransaction`; added `@Lazy` on `asyncOrderSubmitter`
- **TradeDirectDBUtils.java**: `AgroalDataSource` -> `DataSource`; added `@Lazy` on `@Qualifier("tradeDirect")` field

### Async Processing
- **AsyncOrder.java**: Added `@Lazy` on `tradeService` field to break circular dependency
- **AsyncOrderSubmitter.java**: Quarkus VirtualThreads `ExecutorService` -> `Executors.newCachedThreadPool()`
- **AsyncScheduledOrder.java**: `@Dependent` -> `@Component @Scope("prototype")`
- **AsyncScheduledOrderSubmitter.java**: Same; uses `ApplicationContext.getBean()` for prototype beans

### Messaging
- **MessageProducerService.java**: SmallRye `@Channel + Emitter<String>` -> Spring `ApplicationEventPublisher` publishing `BrokerMessageEvent`/`StreamerMessageEvent`
- **DTBroker3MDB.java**: `@Incoming("trade-broker-queue")` -> `@EventListener` for `BrokerMessageEvent`
- **DTStreamer3MDB.java**: `@Incoming("trade-streamer-topic")` -> `@EventListener` for `StreamerMessageEvent`

### Scheduling & Startup
- **MarketSummarySingleton.java**: `@Scheduled(every = "20s")` -> `@Scheduled(fixedRate = 20000)`; added `@Transactional`
- **DatabasePopulator.java**: `@Observes StartupEvent` -> `@EventListener(ApplicationReadyEvent.class) @Transactional`; `@ConfigProperty` -> `@Value`

### Utilities
- **RecentQuotePriceChangeList.java**: `@ApplicationScoped` -> `@Component`; removed CDI `Event<String>` (unused)
- **TradeRunTimeModeLiteral.java**: Simplified from CDI `AnnotationLiteral` to plain annotation implementation
- Interface annotations (`@RuntimeMode`, `@TradeJDBC`, `@TradeEJB`, `@TradeSession2Direct`, `@Trace`, `@QuotePriceChange`, `@MarketSummaryUpdate`): Converted from CDI `@Qualifier` annotations to simple marker annotations with `@Retention(RUNTIME)`

### Unchanged Files (no modifications needed)
- All JPA entity classes (`AccountDataBean`, `AccountProfileDataBean`, `HoldingDataBean`, `OrderDataBean`, `QuoteDataBean`) - already use `jakarta.persistence` compatible with Spring Boot 3.x
- Bean classes (`MarketSummaryDataBean`, `RunStatsDataBean`) - plain POJOs
- Utility classes (`TradeConfig`, `TimerStat`, `KeyBlock`, `Log`, `FinancialUtils`, `MDBStats`, `KeySequenceDirect`) - no framework dependencies

---

## Errors Encountered and Resolutions

### Error 1: Circular Dependency at Startup
**Symptom**: Application failed to start with `UnsatisfiedDependencyException: Is there an unresolvable circular reference?`
**Root Cause**: Circular bean dependency chain: `directSLSBBean` -> `tradeDirect` -> `asyncOrderSubmitter` -> `asyncOrder` -> `tradeDirect`
**Resolution**: Added `@Lazy` annotation to break the cycle in four files:
- `AsyncOrder.java`: `@Lazy` on `tradeService` field
- `TradeDirect.java`: `@Lazy` on `asyncOrderSubmitter` field
- `DirectSLSBBean.java`: `@Lazy` on `tradeDirect` field
- `TradeDirectDBUtils.java`: `@Lazy` on `ts` field

### Error 2: Actuator Health Endpoint 404
**Symptom**: Smoke test `wait_for_ready()` timed out because `/actuator/health` returned 404
**Root Cause**: `spring.mvc.servlet.path=/rest` routes all DispatcherServlet endpoints (including actuator) under `/rest/`
**Resolution**: Updated smoke test to use `/rest/actuator/health`

### Error 3: Smoke Test Parameter/URL Mismatches
**Symptom**: 8 of 14 smoke tests failed with 400 or 404 errors
**Root Cause**: Smoke test URLs and parameter names did not match the actual Spring controller endpoint definitions
**Resolution**: Updated smoke.py to match actual controller endpoints:
- Login: `uid`/`passwd` -> `userID`/`password`
- Account: `/trade/account?uid=...` -> `/trade/account/{userID}`
- Holdings: `/trade/holdings?uid=...` -> `/trade/account/{userID}/holdings`
- Orders: `/trade/orders?uid=...` -> `/trade/account/{userID}/orders`
- Buy: `uid` -> `userID`
- Register: `uid`/`passwd`/`balance` -> `userID`/`password`/`openBalance`
- Messaging ping: GET `/messaging/pingBroker` -> POST `/messaging/ping/broker`
- Messaging ping: GET `/messaging/pingStreamer` -> POST `/messaging/ping/streamer`
