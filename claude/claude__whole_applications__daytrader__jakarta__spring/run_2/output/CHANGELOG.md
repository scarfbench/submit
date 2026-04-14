# CHANGELOG - DayTrader Jakarta EE to Spring Boot Migration

## Overview

Migrated the IBM DayTrader stock trading simulation application from Jakarta EE (Open Liberty) to Spring Boot 3.2.5. The application was originally built on EJB 3.2, JPA 2.2, CDI 2.0, JAX-RS 2.1, JSF 2.3, JMS/MDB, WebSocket, and Servlet APIs running on Open Liberty with Java 8. It now runs as a standalone Spring Boot application on Java 17+.

## Build & Runtime Changes

### pom.xml
- **Replaced**: Open Liberty/Java EE 8 parent and dependencies
- **Added**: Spring Boot 3.2.5 parent (`spring-boot-starter-parent`)
- **Added dependencies**: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-websocket`, H2 database, Jackson databind, JAXB API
- **Changed**: Java version from 8 to 17
- **Changed**: Final artifact name to `daytrader-spring`
- **Removed**: `liberty-maven-plugin`, `maven-war-plugin`, all Java EE/Jakarta EE API dependencies

### Dockerfile
- **Changed**: Base image to `maven:3.9.12-ibm-semeru-21-noble` (Java 21 SDK)
- **Changed**: Build command to `mvn clean package -DskipTests -q`
- **Changed**: Startup command to `java -jar target/daytrader-spring.jar`
- **Added**: Python/Playwright installation for smoke testing

### application.properties (NEW)
- H2 in-memory database configuration (`jdbc:h2:mem:tradedb`)
- JPA/Hibernate settings (`ddl-auto=update`, `open-in-view=true`)
- Jackson JSON serialization settings
- Logging configuration

## Application Code Changes

### New Files

| File | Purpose |
|------|---------|
| `DayTraderApplication.java` | Spring Boot main class with `@SpringBootApplication`, `@EnableScheduling`, `@EnableAsync` |
| `repositories/QuoteRepository.java` | Spring Data JPA repository for quotes with custom query methods |
| `repositories/AccountRepository.java` | Spring Data JPA repository for accounts |
| `repositories/AccountProfileRepository.java` | Spring Data JPA repository for account profiles |
| `repositories/HoldingRepository.java` | Spring Data JPA repository for holdings |
| `repositories/OrderRepository.java` | Spring Data JPA repository for orders with custom query methods |
| `service/TradeService.java` | Core trading business logic (replaces EJB `TradeSLSBBean`) |
| `service/TradeDBService.java` | Database initialization and reset operations |
| `web/rest/HealthController.java` | `GET /api/health` endpoint |
| `web/rest/QuoteController.java` | Quote CRUD REST endpoints |
| `web/rest/AccountController.java` | Account/profile/holdings/orders REST endpoints |
| `web/rest/TradeController.java` | Buy/sell trade execution REST endpoints |
| `web/rest/MarketController.java` | Market summary REST endpoint |
| `web/rest/ConfigController.java` | Configuration and DB management REST endpoints |
| `smoke.py` | Python smoke test suite (15 tests) |

### Modified Files

| File | Changes |
|------|---------|
| `entities/AccountDataBean.java` | `javax.persistence` -> `jakarta.persistence`; added `@JsonIgnore` on collections; removed EJBException; fixed deprecated `new Integer()` |
| `entities/AccountProfileDataBean.java` | `javax.persistence` -> `jakarta.persistence`; added `@JsonIgnore` on account; implements `Persistable<String>` for Spring Data JPA compatibility with assigned IDs |
| `entities/QuoteDataBean.java` | `javax.persistence` -> `jakarta.persistence`; added `@JsonIgnoreProperties(ignoreUnknown=true)` |
| `entities/HoldingDataBean.java` | `javax.persistence` -> `jakarta.persistence`; added `@JsonIgnoreProperties` on account; fixed deprecated `new Integer()` |
| `entities/OrderDataBean.java` | `javax.persistence` -> `jakarta.persistence`; added `@JsonIgnoreProperties` on account/holding; fixed deprecated `new Integer()` |
| `beans/MarketSummaryDataBean.java` | Removed `javax.json.*` imports (JSON-P dependency removed) |
| `util/TradeConfig.java` | Fixed deprecated `new Float()` and `new Integer()` constructors; fixed `BigDecimal.ROUND_HALF_UP` -> `RoundingMode.HALF_UP`; fixed no-op `setScale()` calls |
| `util/FinancialUtils.java` | Added `RoundingMode.HALF_UP` constant alongside legacy `ROUND` int constant |

### Deleted Files

| Category | Files Removed |
|----------|--------------|
| EJB Beans | `impl/TradeSLSBBean.java`, `impl/TradeEJB3BeanService.java` |
| EJB Interfaces | `interfaces/TradeServices.java` |
| MDB | `mdb/DTBroker3MDB.java` |
| JAX-RS | `jaxrs/QuoteResource.java`, `jaxrs/AccountResource.java`, `jaxrs/MarketSummaryResource.java`, `jaxrs/TradeApp.java` |
| JSF | `web/jsf/TradeAppJSF.java`, `web/jsf/TradeConfigJSF.java`, `web/jsf/OrdersAlertFilter.java`, `web/jsf/PortfolioJSF.java`, `web/jsf/QuoteJSF.java`, all XHTML files |
| Servlets | `web/prims/*`, `web/servlet/*` |
| WebSocket | `web/websocket/*` |
| CDI | `beans/MDBStats.java`, `util/TraceInterceptor.java` |
| Liberty Config | `server.xml`, `web.xml`, `persistence.xml`, `ibm-web-ext.xml` |
| Static Resources | JSP files, CSS, images, JavaScript |

## Architecture Changes

### Dependency Injection
- **Before**: CDI 2.0 (`@Inject`, `@ApplicationScoped`)
- **After**: Spring DI (`@Autowired`, `@Service`, `@RestController`)

### Business Logic Layer
- **Before**: EJB Stateless Session Bean (`TradeSLSBBean`) with `@Stateless`
- **After**: Spring `@Service` with `@Transactional` (`TradeService`)

### REST API Layer
- **Before**: JAX-RS 2.1 (`@Path`, `@GET`, `@POST`)
- **After**: Spring MVC (`@RestController`, `@GetMapping`, `@PostMapping`)

### Data Access Layer
- **Before**: JPA 2.2 via `EntityManager` with JPQL/native queries
- **After**: Spring Data JPA repositories with derived query methods and `@Query` annotations

### Messaging
- **Before**: JMS 2.0 with MDB (`DTBroker3MDB`) for async order processing
- **After**: Synchronous order processing within `TradeService` (no JMS equivalent needed for this workload)

### UI Layer
- **Before**: JSF 2.3 with XHTML Facelets, Servlets, JSP
- **After**: REST API only (UI removed; all functionality accessible via REST endpoints)

### Database
- **Before**: H2 via Liberty JNDI datasource
- **After**: H2 via Spring Boot auto-configuration (`spring.datasource.*`)

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/health` | Health check |
| GET | `/api/config` | Application configuration |
| POST | `/api/config/buildDB` | Initialize database with test data |
| POST | `/api/config/resetTrade` | Reset trade statistics |
| GET | `/api/quotes` | List all quotes |
| GET | `/api/quotes/{symbol}` | Get quote by symbol |
| POST | `/api/quotes` | Create a new quote |
| POST | `/api/account/register` | Register a new user |
| POST | `/api/account/login` | Login user |
| POST | `/api/account/logout` | Logout user |
| GET | `/api/account/{userID}` | Get account data |
| GET | `/api/account/{userID}/profile` | Get account profile |
| PUT | `/api/account/{userID}/profile` | Update account profile |
| GET | `/api/account/{userID}/holdings` | Get user holdings |
| GET | `/api/account/{userID}/orders` | Get user orders |
| POST | `/api/trade/buy` | Execute buy order |
| POST | `/api/trade/sell` | Execute sell order |
| GET | `/api/trade/orders/{userID}/closed` | Get closed orders |
| GET | `/api/market/summary` | Get market summary |

## Smoke Test Results

All 15 smoke tests pass:

```
Phase 1: Health and Configuration     - 2/2 PASS
Phase 2: Database Setup               - 1/1 PASS
Phase 3: Quote Operations             - 3/3 PASS
Phase 4: User Operations              - 4/4 PASS
Phase 5: Trading Operations           - 3/3 PASS
Phase 6: Market Operations            - 1/1 PASS
Phase 7: Session Management           - 1/1 PASS
Total: 15/15 passed, 0/15 failed
```

## Key Technical Decisions

1. **Persistable Interface**: `AccountProfileDataBean` implements `Persistable<String>` because its `@Id` is a user-assigned `String` (not auto-generated). Without this, Spring Data JPA's `save()` method calls `merge()` instead of `persist()` for new entities, causing `TransientPropertyValueException`.

2. **No JMS Migration**: The MDB-based async order processing was replaced with synchronous processing within `TradeService`. The DayTrader workload does not require async processing for correctness.

3. **REST-Only API**: The JSF/Servlet UI layer was removed entirely. All application functionality is exposed through REST endpoints, making the application suitable for headless/API-first deployment.

4. **Entity Save Order**: Profile entities must be persisted before Account entities (which own the FK). The managed entity reference from `save()` must be used for subsequent associations to avoid Hibernate session conflicts.
