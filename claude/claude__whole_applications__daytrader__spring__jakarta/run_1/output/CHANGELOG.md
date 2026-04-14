# CHANGELOG: Spring Boot to Jakarta EE Migration

## Migration Summary

Migrated DayTrader application from **Spring Boot 3.3.0** to **Jakarta EE 10** running on **Open Liberty**.

- **Source framework**: Spring Boot 3.3.0 (spring-boot-starter-artemis, cache, data-jpa, jersey, validation, web, websocket)
- **Target framework**: Jakarta EE 10 (CDI 4.0, EJB 4.0, JPA 3.1, JMS 3.1, Servlet 6.0, WebSocket 2.1)
- **Runtime**: Open Liberty 26.0.0.2 (full-java17-openj9-ubi)
- **Files modified**: 69+ Java source files, 6 XML configuration files, 1 Dockerfile, 1 pom.xml
- **Smoke tests**: 17/17 passing

---

## Phase 1: Analysis

**Action**: Analyzed the entire codebase (139 Java files, 23 JSPs) to identify all Spring-specific dependencies and patterns.

**Findings**:
- 69 files with `org.springframework` imports
- Spring patterns found: `@Component`, `@Service`, `@Autowired`, `@Configuration`, `@Bean`, `@Transactional`, `@JmsListener`, `@Scheduled`, `@Async`, `@EventListener`, `ApplicationContext`, `JmsTemplate`, `SpringBeanAutowiringSupport`, `ApplicationEventPublisher`
- H2 embedded database
- JMS via Spring JmsTemplate with ActiveMQ Artemis
- WebSocket via Spring endpoint configurator

---

## Phase 2: Build Configuration (pom.xml)

**Action**: Rewrote pom.xml to remove all Spring dependencies and add Jakarta EE 10 API.

**Changes**:
- Removed Spring Boot parent (`spring-boot-starter-parent:3.3.0`)
- Removed all `spring-boot-starter-*` dependencies (artemis, cache, data-jpa, jersey, validation, web, websocket)
- Added `jakarta.jakartaee-api:10.0.0` (provided scope - supplied by Liberty)
- Kept H2 2.2.224, commons-lang3 3.18.0, commons-beanutils 1.11.0
- Changed packaging to WAR with finalName `daytrader`
- Set Java version to 17 (matching available Open Liberty images)
- Replaced `spring-boot-maven-plugin` with `maven-compiler-plugin` (3.13.0) and `maven-war-plugin` (3.4.0)

---

## Phase 3: Configuration Files

### server.xml (Liberty configuration)
- Features: webProfile-10.0, messaging-3.1, messagingServer-3.0, messagingClient-3.0, concurrent-3.0, mdb-4.0, localConnector-1.0
- H2 datasource at `jdbc/TradeDataSource` (file-based, `DB_CLOSE_ON_EXIT=FALSE`)
- JMS connection factories, queues (`TradeBrokerQueue`), and topics (`TradeStreamerTopic`)
- JMS activation specs for MDBs: `daytrader/DTBroker3Listener`, `daytrader/DTStreamer3Listener`
- `ManagedExecutorService` and `ManagedScheduledExecutorService` for async operations
- HTTP on port 9080, HTTPS on port 9443

### web.xml
- Updated to Jakarta EE 6.0 (`web-app_6_0.xsd`)
- Changed `res-auth` from `Container` to `Application` for `jdbc/TradeDataSource` (H2 requires empty password which Liberty container auth rejects)

### beans.xml
- CDI 4.0 namespace (`https://jakarta.ee/xml/ns/jakartaee`)
- `bean-discovery-mode="all"`

### persistence.xml
- JPA 3.1 namespace, JTA transaction type with `jdbc/TradeDataSource`
- EclipseLink DDL generation: `create-tables`

### Dockerfile
- Multi-stage build: `maven:3.9.12-eclipse-temurin-17` (builder) + `open-liberty:full-java17-openj9-ubi` (runtime)
- H2 jar staged from Maven cache to Liberty's `/config/lib/`
- System Python 3.9 venv with pytest, requests, Playwright for smoke testing
- Uses `dnf` (RHEL 8 UBI) instead of `apt-get`

---

## Phase 4: Java Source File Migration

### Annotation Mapping (Spring -> Jakarta EE)

| Spring Pattern | Jakarta EE Replacement |
|---|---|
| `@SpringBootApplication` | `@ApplicationScoped` |
| `@Component` | Removed (CDI manages beans via `beans.xml` discovery) |
| `@Service("name")` | `@Stateless @Named("name")` or `@ApplicationScoped @Named("name")` |
| `@Autowired` | `@Inject` or `@Resource` (for JNDI resources) |
| `@Configuration @Bean` | CDI `@Produces @Named` or removed (Liberty server.xml) |
| `@Transactional` (on EJBs) | `@TransactionAttribute(TransactionAttributeType.REQUIRED)` |
| `@Scheduled(fixedRate=...)` | `@Schedule(second="*/N", minute="*", hour="*", persistent=false)` |
| `@JmsListener(destination=...)` | `@MessageDriven(activationConfig={...})` + `MessageListener` |
| `JmsTemplate.send()` | `JMSContext.createProducer().send()` via `@Resource ConnectionFactory` |
| `ApplicationEventPublisher` | CDI `Event<T>.fire()` |
| `@EventListener @Async` | CDI `@Observes` |
| `SpringBeanAutowiringSupport` | Removed (CDI injects into servlets/filters natively) |
| `ApplicationContext.getBean()` | CDI `Instance<T>` with `@Any` qualifier and annotation iteration |
| `@Component @SessionScope` | `@Named @SessionScoped` |
| `@Component @RequestScope` | `@Named @RequestScoped` |

### Key Files Modified

#### EJB/Service Layer
- **TradeSLSBBean.java**: `@Service` -> `@Stateless @Named("Full EJB3") @TradeEJB`, JmsTemplate -> JMSContext, `@Transactional` -> `@TransactionAttribute`
- **DirectSLSBBean.java**: Same `@Transactional` -> `@TransactionAttribute` fix
- **TradeDirect.java**: `@Component @RequestScope` -> `@ApplicationScoped @Named("Direct (JDBC)") @TradeJDBC`, JmsTemplate -> JMS API
- **MarketSummarySingleton.java**: `@Service` -> `@Singleton @Startup @ConcurrencyManagement(BEAN)`, `@Scheduled` -> `@Schedule`, `ApplicationEventPublisher` -> CDI `Event<T>`

#### JMS Message-Driven Beans
- **DTBroker3Listener.java**: Spring `@JmsListener` -> `@MessageDriven` MDB implementing `MessageListener`
- **DTStreamer3Listener.java**: Same conversion (topic subscriber)

#### Web/Servlet Layer
- **TradeServletAction.java**: `@Component @SessionScope` -> `@Named @SessionScoped`, `ApplicationContext` -> CDI `@Inject @Any Instance<TradeServices>`
- **OrdersAlertFilter.java**: Added `@Any` qualifier to `Instance<TradeServices>`, removed `SpringBeanAutowiringSupport`
- **~50 Ping/test servlets**: Removed `@Component`, `SpringBeanAutowiringSupport`, replaced `@Autowired` with `@Inject`/`@Resource`

#### Config Classes
- **AsyncConfig.java**: `@Configuration @Bean` -> CDI `@Produces @Named`, replaced `Thread.ofPlatform()` (Java 21) with Java 17 lambda
- **DaytraderApplication.java**: `@SpringBootApplication` -> `@ApplicationScoped`
- **JmsConfig.java**, **DualPortConfig.java**, **WebSocketEndpointConfig.java**, **ExecutorConfig.java**: Empty stubs (Liberty server.xml handles configuration)

---

## Phase 5: Runtime Issues Resolved

### Issue 1: `@Transactional` not allowed on EJB beans
- **Files**: `TradeSLSBBean.java`, `DirectSLSBBean.java`
- **Error**: `CWOWB2000E: The annotation @jakarta.transaction.Transactional is not allowed on EJB`
- **Fix**: Replaced with `@TransactionAttribute(TransactionAttributeType.REQUIRED)`

### Issue 2: `TX_SUPPORTS` not allowed on Singleton EJB
- **File**: `MarketSummarySingleton.java`
- **Error**: `CNTR0089E: Transaction attribute TX_SUPPORTS is not allowed for method "updateMarketSummary"`
- **Fix**: Added `@ConcurrencyManagement(ConcurrencyManagementType.BEAN)`, changed to `NOT_SUPPORTED`

### Issue 3: CDI `Instance<TradeServices>` returned empty
- **Files**: `TradeServletAction.java`, `OrdersAlertFilter.java`
- **Error**: `No TradeServices bean named 'Full EJB3' (available: [])`
- **Fix**: Added `@Any` qualifier to `Instance<TradeServices>` injection points (beans had custom qualifiers `@TradeEJB`, `@TradeJDBC`, `@TradeSession2Direct`)

### Issue 4: H2 empty password rejected by container auth
- **File**: `web.xml`, `server.xml`
- **Error**: `CWWKS1301E: The attribute password must be defined`
- **Fix**: Changed `res-auth` from `Container` to `Application` in web.xml

### Issue 5: `Thread.ofPlatform()` (Java 21 API)
- **File**: `AsyncConfig.java`
- **Fix**: Replaced with Java 17 compatible `ThreadFactory` lambda

### Issue 6: H2 `AUTO_SERVER=TRUE` unsupported
- **File**: `server.xml`
- **Fix**: Removed `AUTO_SERVER=TRUE` from JDBC URL

### Issue 7: Dockerfile package manager mismatch
- **Error**: `microdnf: command not found` / `apt-get: command not found`
- **Fix**: Open Liberty UBI image uses `dnf` (RHEL 8)

### Issue 8: Python greenlet compilation failure
- **Error**: `uv` downloaded Python 3.14 causing C++ ABI issues with greenlet
- **Fix**: Used system Python 3.9 with standard `python3 -m venv`

---

## Phase 6: Smoke Test Results

All 17 smoke tests pass:

```
Smoke Test Results: 17/17 passed, 0 failed
```

| Test | Result |
|---|---|
| Welcome page | PASS |
| Config page | PASS |
| Build DB (populate) | PASS |
| PingServlet | PASS |
| PingServletWriter | PASS |
| PingServletCDI | PASS |
| PingJSONPObject | PASS |
| PingJDBCRead | PASS |
| User registration | PASS |
| User login | PASS |
| Home page | PASS |
| Portfolio | PASS |
| Quotes | PASS |
| Account page | PASS |
| Market summary | PASS |
| Buy stock | PASS |
| Logout | PASS |

## Verification

- Zero `org.springframework` imports remaining in codebase
- Zero `@jakarta.transaction.Transactional` annotations remaining on EJBs
- Application starts in ~5 seconds on Open Liberty
- All CRUD operations (register, login, buy, quote, portfolio) functional
- JPA/EclipseLink persistence working with H2
- CDI injection working across all layers (EJB, servlet, filter, websocket, MDB)
