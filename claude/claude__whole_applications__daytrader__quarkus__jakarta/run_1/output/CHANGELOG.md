# CHANGELOG - DayTrader Quarkus to Jakarta EE Migration

## Migration Summary

Migrated IBM DayTrader from Quarkus to Jakarta EE 10 running on Open Liberty.

### Build & Runtime

| Aspect | Before (Quarkus) | After (Jakarta EE) |
|--------|------------------|---------------------|
| Runtime | Quarkus | Open Liberty (kernel-slim-java17-openj9) |
| Packaging | uber-jar | WAR |
| Java EE API | jakarta.* via Quarkus | Jakarta EE 10 API (provided) |
| Database | H2 (Quarkus-managed) | H2 2.2.224 (Liberty DataSource) |
| JPA Provider | Hibernate (Quarkus) | EclipseLink (Open Liberty) |
| REST Framework | Quarkus RESTEasy | JAX-RS 3.1 (RESTEasy on Liberty) |
| Messaging | SmallRye Reactive Messaging | CDI Events (fireAsync/ObservesAsync) |
| Scheduling | Quarkus Scheduler | Jakarta EJB Timer (@Schedule) |
| JSON Serialization | Jackson | JSON-B (jakarta.json.bind) |
| DI Framework | CDI (Quarkus) | CDI 4.0 + EJB (Liberty) |

### Changes Made

#### 1. Project Configuration (`pom.xml`)
- Changed packaging from uber-jar to WAR
- Replaced Quarkus BOM/plugins with Jakarta EE 10 API dependency (provided scope)
- Added H2 2.2.224 as provided dependency
- Added maven-dependency-plugin to copy H2 jar for Liberty shared resources
- Added liberty-maven-plugin for Open Liberty support
- Added maven-war-plugin configuration

#### 2. Application Server Configuration
- **Created `src/main/liberty/config/server.xml`**: Open Liberty server configuration
  - `jakartaee-10.0` feature for full Jakarta EE 10 support
  - H2 in-memory DataSource at `jdbc/TradeDataSource`
  - Basic user registry for EJB support
  - Web application deployment with third-party API visibility

#### 3. JPA / Persistence
- **Updated `persistence.xml`**:
  - Changed to JPA 3.0 namespace
  - Set `eclipselink.target-database` to `H2Platform` for proper DDL generation
  - Used `drop-and-create` schema generation strategy
- **Changed `GenerationType.IDENTITY` to `GenerationType.AUTO`** in:
  - `AccountDataBean.java` (ACCOUNTID)
  - `HoldingDataBean.java` (HOLDINGID)
  - `OrderDataBean.java` (ORDERID)
  - Reason: EclipseLink generates `INTEGER IDENTITY` SQL syntax for `IDENTITY` strategy, which H2 2.2.224 rejects. `AUTO` uses TABLE-based generation compatible with H2.

#### 4. Entity Changes
- **Jackson to JSON-B**: Replaced `@JsonIgnore` (com.fasterxml.jackson) with `@JsonbTransient` (jakarta.json.bind) in:
  - `AccountDataBean.java`
  - `AccountProfileDataBean.java`
  - `HoldingDataBean.java`
  - `OrderDataBean.java`
- **Changed `AccountProfileDataBean.account` fetch type** from `LAZY` to `EAGER` on the `@OneToOne(mappedBy)` inverse relationship to improve compatibility with EclipseLink

#### 5. Framework Migration
- **DataSource injection** (`TradeDirectDBUtils.java`):
  - `io.agroal.api.AgroalDataSource` with `@Inject` -> `javax.sql.DataSource` with `@Resource(lookup="jdbc/TradeDataSource")`
- **Startup** (`DatabasePopulator.java`):
  - Changed from Quarkus `@Startup` CDI event observer to EJB `@Singleton @Startup` with `@PostConstruct`
  - Changed from Container-Managed Transactions (CMT) to Bean-Managed Transactions (BMT) with `@TransactionManagement(TransactionManagementType.BEAN)` and manual `UserTransaction`
  - Reason: CMT singleton `@PostConstruct` cannot recover from transaction rollback when tables don't exist yet during `drop-and-create`
- **Scheduling** (`MarketSummarySingleton.java`):
  - `@io.quarkus.scheduler.Scheduled` -> Jakarta EJB `@Schedule`
- **Messaging** (`MessageProducerService.java`):
  - SmallRye Reactive Messaging channels -> CDI `Event.fireAsync()` with `@ObservesAsync`
- **Virtual Threads** removal: Removed `@io.quarkus.virtual.threads.VirtualThreads` annotations

#### 6. EJB Service Layer Fix (`TradeSLSBBean.java`)
- **Default runtime mode**: Changed from `DIRECT_JPA` (no implementation exists) to `EJB3` (maps to `TradeSLSBBean`)
- **Account lookup**: Replaced `profile.getAccount()` calls with JPQL query `findAccountByUserID(userID)` to work around EclipseLink not reliably loading inverse `@OneToOne(mappedBy)` relationships on Open Liberty

#### 7. New JAX-RS Resources
- **`TradeResource.java`** (`/rest/trade`): Market, buy, sell, login, logout, account, profile, holdings, orders, register, resetTrade
- **`QuoteResource.java`** (`/rest/quotes`): Get quotes by symbol(s), get all quotes
- **`MessagingResource.java`** (`/rest/messaging`): Broker and streamer ping endpoints
- **`TradeAppResource.java`** (`/rest/app`): HTML landing page with API links
- **`TradeApplication.java`** (`/rest`): JAX-RS application path

#### 8. Session-to-Direct Delegate
- **Created `DirectSLSBBean.java`**: Implements `TradeServices` with `@RuntimeMode("Session to Direct")`, delegates to `TradeDirect` JDBC implementation

#### 9. Dockerfile
- Multi-stage build: Maven 3.9.9 (JDK 17) build stage -> Open Liberty kernel-slim-java17-openj9-ubi runtime
- Copies server.xml, H2 JDBC driver, WAR file, and smoke.py
- Runs `features.sh` and `configure.sh` for Liberty optimization

### Errors Encountered and Resolved

1. **Derby `EmbeddedXADataSource40` ClassNotFoundException**: Derby 10.16+ removed this class. Switched to H2 database.
2. **EJB user registry missing** (`CWWKS9660E`): Added `<basicRegistry>` to server.xml.
3. **CMT transaction rollback in `@PostConstruct`**: Singleton startup bean couldn't recover from failed SELECT on non-existent table. Switched to Bean-Managed Transactions.
4. **EclipseLink `INTEGER IDENTITY` syntax error with H2 2.2.224**: Changed `GenerationType.IDENTITY` to `GenerationType.AUTO` in all entity ID fields.
5. **No `@RuntimeMode("Direct (JPA)")` implementation**: Changed default runtime mode to `EJB3`.
6. **`profile.getAccount()` returns null on EclipseLink/Liberty**: Inverse `@OneToOne(mappedBy)` not loaded reliably. Added JPQL query helper method `findAccountByUserID()`.

### Smoke Test Results

All 12/12 smoke tests pass:
- Market Summary
- Get Quote
- Get Multiple Quotes
- Login
- Get Account
- Get Account Profile
- Get Holdings
- Buy Stock
- Get Orders
- Register New User
- Logout
- Web App
