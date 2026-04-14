# DayTrader Migration: Quarkus -> Jakarta EE (Open Liberty)

## Summary

Complete migration of the DayTrader stock trading application from **Quarkus 3.17** to **Jakarta EE 10** running on **Open Liberty** with EclipseLink JPA and H2 in-memory database.

**Smoke Test Results: 12/12 PASSED**

---

## Framework Changes

### Build System (pom.xml)
- Replaced Quarkus BOM, plugins, and dependencies with standard Maven WAR packaging
- Added `jakarta.jakartaee-api:10.0.0` (provided scope)
- Added `org.eclipse.microprofile.config:microprofile-config-api:3.1` (provided scope)
- Changed H2 dependency from `runtime` to `compile` scope (bundled in WAR for Liberty)
- Added `jackson-databind` (provided by Liberty)
- Added `liberty-maven-plugin` for Open Liberty integration
- Removed all Quarkus-specific dependencies (quarkus-bom, quarkus-arc, quarkus-hibernate-orm, quarkus-resteasy, quarkus-smallrye-reactive-messaging, etc.)

### Application Server Configuration
- **NEW** `src/main/liberty/config/server.xml` - Open Liberty server configuration
  - Features: jakartaee-10.0, mpConfig-3.1, localConnector-1.0
  - H2 JDBC DataSource with MODE=LEGACY for EclipseLink compatibility
  - JMS messaging engine with Queue (TradeBrokerQueue) and Topic (TradeStreamerTopic)
  - JMS activation specs for MDBs
  - Basic user registry (required for EJB container)
  - Web application deployment configuration

### Persistence (JPA)
- **NEW** `src/main/resources/META-INF/persistence.xml`
  - JTA transaction type with `jdbc/TradeDataSource` JNDI lookup
  - EclipseLink as JPA provider (Liberty default) with H2Platform target database
  - DDL generation disabled (`none`) - tables created programmatically
- **REMOVED** `application.properties` (Quarkus-specific Hibernate configuration)

### Web Configuration
- **NEW** `src/main/webapp/WEB-INF/web.xml` - Standard Jakarta EE web descriptor
- **NEW** `src/main/webapp/WEB-INF/beans.xml` - CDI bean discovery (annotated mode)
- **NEW** `src/main/resources/META-INF/microprofile-config.properties` - MicroProfile Config defaults

### Dockerfile
- Multi-stage build: Maven 3.9 (builder) -> Open Liberty `full-java17-openj9-ubi`
- Installs Python/uv/requests for smoke test execution
- Copies server.xml, H2 JAR, WAR, and smoke.py into Liberty directories

---

## Code Changes

### New Files
- `src/main/java/com/ibm/websphere/samples/daytrader/jaxrs/JaxrsApplication.java`
  - JAX-RS Application class with `@ApplicationPath("/rest")`
- `smoke.py` - 12-endpoint smoke test suite

### Messaging (SmallRye Reactive -> JMS)
- **MessageProducerService.java**: Replaced `@Channel`/`@Broadcast`/`Emitter` with JMS `@Resource` ConnectionFactory/Queue/Topic using JMSContext
- **DTBroker3MDB.java**: Replaced `@ApplicationScoped` + `@Incoming("trade-broker-queue")` with `@MessageDriven` implementing `MessageListener`, receiving JMS TextMessage
- **DTStreamer3MDB.java**: Same pattern as DTBroker3MDB but for Topic subscription

### Startup/Scheduling
- **DatabasePopulator.java**: Replaced Quarkus `StartupEvent` listener with `@Singleton @Startup` EJB
  - Uses Bean-Managed Transactions (`@TransactionManagement(BMT)`) with `UserTransaction`
  - Creates all database tables via JPA native queries (`em.createNativeQuery("CREATE TABLE IF NOT EXISTS ...")`)
  - Populates quotes and users in separate transactions
  - Required because EclipseLink's H2Platform DDL uses incompatible H2 1.x syntax
- **MarketSummarySingleton.java**: Replaced `@ApplicationScoped` + Quarkus `@Scheduled(every = "20s")` with `@Singleton` + EJB `@Schedule(second = "*/20", ...)`
  - Replaced synchronized methods with `@Lock(LockType.READ/WRITE)`
  - Changed `@Inject EntityManager` to `@PersistenceContext(unitName = "daytrader")`

### Concurrency
- **AsyncOrderSubmitter.java**: Replaced `@VirtualThreads ExecutorService` with `@Resource ManagedExecutorService`
- **AsyncScheduledOrderSubmitter.java**: Same pattern as AsyncOrderSubmitter

### Data Access
- **TradeSLSBBean.java**:
  - Changed `@Inject EntityManager` to `@PersistenceContext(unitName = "daytrader") EntityManager`
  - Added `getAccountForProfile(String userID)` helper method using JPQL query to replace `profile.getAccount()` calls (EclipseLink inverse `@OneToOne(mappedBy)` doesn't resolve without weaving)
  - Replaced all 5 `profile.getAccount()` calls with `getAccountForProfile(userID)`
  - Added `entityManager.flush()` after `persist()` in `createOrder()` and `createHolding()` to ensure IDENTITY-generated IDs are populated before use
- **TradeDirect.java**: Replaced `QuarkusTransaction` with `@Resource UserTransaction`, replaced `@Inject DataSource` with `@Resource(lookup = "jdbc/TradeDataSource") DataSource`
- **TradeDirectDBUtils.java**: Replaced `AgroalDataSource` with standard `javax.sql.DataSource` via `@Resource(lookup = "jdbc/TradeDataSource")`

### Entity Changes
- **AccountDataBean.java**, **AccountProfileDataBean.java**, **HoldingDataBean.java**, **OrderDataBean.java**:
  - Added `@JsonbTransient` alongside `@JsonIgnore` on all relationship fields (Liberty uses JSONB by default, not Jackson, for JAX-RS serialization)
- **AccountProfileDataBean.java**: Changed `@OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)` to `FetchType.EAGER`

### Removed Files
- `src/test/java/com/ibm/websphere/samples/daytrader/TradeResourceTest.java` (Quarkus test)
- `src/test/java/com/ibm/websphere/samples/daytrader/MessagingTest.java` (Quarkus test)
- `src/test/java/com/ibm/websphere/samples/daytrader/MessagingResourceTest.java` (Quarkus test)
- `src/main/resources/application.properties` (Quarkus config)

---

## Key Technical Decisions

### H2 2.x Compatibility with EclipseLink
EclipseLink's `H2Platform` generates `INTEGER IDENTITY` DDL and uses `CALL IDENTITY()` for auto-generated IDs, both of which are H2 1.x features removed in H2 2.x. Two fixes applied:
1. **`MODE=LEGACY`** in JDBC URL restores H2 1.x function compatibility
2. **DDL generation disabled** (`eclipselink.ddl-generation=none`) - tables created via native SQL with correct `GENERATED BY DEFAULT AS IDENTITY` syntax

### @OneToOne Inverse Relationship
EclipseLink on Liberty (without byte-code weaving) doesn't properly resolve inverse `@OneToOne(mappedBy=...)` relationships. Account lookup via `profile.getAccount()` returned null. Fixed by adding a JPQL query helper method.

### JSONB vs Jackson Serialization
Liberty's JAX-RS uses JSON-B (not Jackson) by default. Entity fields annotated with `@JsonIgnore` (Jackson) were still being serialized, causing circular reference errors. Fixed by adding `@JsonbTransient` annotations.

### Bean-Managed Transactions for Startup
Liberty's `@Singleton @Startup` with Container-Managed Transactions had issues with rollback handling in `@PostConstruct`. Switched to Bean-Managed Transactions for full control over transaction boundaries during database initialization.

---

## Validation

### Smoke Tests (12/12 PASSED)
| # | Test | Method | Endpoint | Status |
|---|------|--------|----------|--------|
| 1 | Market Summary | GET | /rest/trade/market | PASS |
| 2 | Get Quote | GET | /rest/quotes/s:0 | PASS |
| 3 | Get Multiple Quotes | GET | /rest/quotes/s:0,s:1,s:2 | PASS |
| 4 | Login | POST | /rest/trade/login | PASS |
| 5 | Get Account | GET | /rest/trade/account/uid:0 | PASS |
| 6 | Get Account Profile | GET | /rest/trade/account/uid:0/profile | PASS |
| 7 | Get Holdings | GET | /rest/trade/account/uid:0/holdings | PASS |
| 8 | Get Orders | GET | /rest/trade/account/uid:0/orders | PASS |
| 9 | Buy Stock | POST | /rest/trade/buy | PASS |
| 10 | Register | POST | /rest/trade/register | PASS |
| 11 | Logout | POST | /rest/trade/logout/uid:0 | PASS |
| 12 | Web App Welcome | GET | /rest/app | PASS |

### Runtime Verification
- Open Liberty starts in ~6 seconds
- Database tables created and populated at startup (100 quotes, 50 users)
- JMS messaging infrastructure (Queue + Topic) operational
- MDB activation specs connected to messaging engine
- All Jakarta EE 10 features loaded successfully
