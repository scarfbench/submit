# CHANGELOG - DayTrader Spring Boot to Jakarta EE Migration

## Migration Summary

Migrated DayTrader application from **Spring Boot 3.3** to **Jakarta EE 10 on Open Liberty 26.x**.

All Spring dependencies have been removed. Zero `org.springframework` imports remain in the codebase.

### Smoke Test Results
- 5/5 tests passing
- Welcome page, config page, app servlet, scenario servlet, and ping servlet all operational

---

## Build & Runtime

### pom.xml
- Removed Spring Boot parent POM (`spring-boot-starter-parent:3.3.0`)
- Removed all `spring-boot-starter-*` dependencies (web, data-jpa, artemis, websocket, validation, actuator)
- Added `jakarta.jakartaee-api:10.0.0` (provided scope)
- Added `microprofile:6.1` (provided scope)
- Added `h2:2.2.224` (runtime scope)
- Added `commons-lang3`, `commons-beanutils`, `parsson` (JSON-P implementation)
- Configured `maven-war-plugin` for WAR packaging
- Configured `liberty-maven-plugin:3.10.3` with `openliberty-runtime:24.0.0.6`

### Dockerfile
- Build stage: `maven:3.9.12-ibm-semeru-21-noble`
- Runtime stage: `icr.io/appcafe/open-liberty:full-java17-openj9-ubi`
- Multi-stage build: Maven compiles WAR, Liberty runtime deploys it
- H2 JAR copied from Maven local repo to `/config/resources/h2.jar`
- Schema init script at `/app/src/main/resources/schema-h2.sql`
- Python3 installed for smoke test execution

### server.xml (Open Liberty Configuration)
- Features: `webProfile-10.0`, `pages-3.1`, `persistence-3.1`, `enterpriseBeansLite-4.0`, `concurrent-3.0`, `websocket-2.1`, `restfulWS-3.1`, `messaging-3.1`, `messagingClient-3.0`, `messagingServer-3.0`, `mdb-4.0`, `connectors-2.1`, `localConnector-1.0`
- DataSource: H2 file-based with JNDI `jdbc/TradeDataSource`
- JMS: Embedded messaging engine with queues, topics, connection factories, and MDB activation specs
- HTTP on port 9080, HTTPS on port 9443

---

## Java Source Changes

### Dependency Injection
| Spring Pattern | Jakarta EE Replacement |
|---|---|
| `@Autowired` | `@Inject` (CDI) |
| `@Component` | `@ApplicationScoped` or removed (servlets auto-discovered) |
| `@Service("name")` | `@ApplicationScoped @Named("name")` |
| `@Component @Scope("prototype")` | `@Dependent` |
| `@SessionScope` | `@SessionScoped` |
| `@RequestScope` | `@RequestScoped` |
| `@Configuration` | `@ApplicationScoped` |
| `@Autowired Map<String, TradeServices>` | CDI `@Produces` method in `TradeServicesProducer` |

### New File: `TradeServicesProducer.java`
Created CDI producer (`config/TradeServicesProducer.java`) that builds `Map<String, TradeServices>` from all `@Named` implementations using CDI `Instance.Handle` API with bean qualifier metadata.

### JMS
| Spring Pattern | Jakarta EE Replacement |
|---|---|
| `JmsTemplate` | Manual JMS: `@Resource ConnectionFactory`, `Session`, `MessageProducer` |
| `@JmsListener(destination=...)` | `@MessageDriven` MDB with activation config properties |
| Spring `JmsConfig` | Liberty `server.xml` JMS configuration |

### Events
| Spring Pattern | Jakarta EE Replacement |
|---|---|
| `ApplicationEventPublisher.publishEvent()` | `@Inject Event<T>` + `event.fire()` |
| `@EventListener` | `@Observes` |
| `@EventListener @Async` | `@ObservesAsync` |

### Scheduling & Concurrency
| Spring Pattern | Jakarta EE Replacement |
|---|---|
| `@Scheduled(fixedRate=...)` | `@Schedule(hour="*", minute="*", second="*/20")` (EJB Timer) |
| `@Async` | `@Resource ManagedExecutorService` |
| `TaskScheduler` | `@Resource ManagedScheduledExecutorService` |
| Spring `ObjectFactory<T>` | CDI `Instance<T>` |

### Transactions
| Spring Pattern | Jakarta EE Replacement |
|---|---|
| `@Transactional` (on CDI beans) | `@jakarta.transaction.Transactional` |
| `@Transactional` (on EJB `@Singleton`) | Removed (EJBs are transactional by default) |

### Servlets & Filters
- Removed `SpringBeanAutowiringSupport.processInjectionBasedOnServletContext()` from all servlets
- Servlets use `@WebServlet` annotations (already present), no Spring `@Component` needed
- `OrdersAlertFilter`: Removed Spring context lookups, uses `@Inject` for CDI beans
- `TradeWebContextListener`: Changed from Spring `ApplicationContextInitializer` to `@Singleton @Startup` EJB

### WebSocket
- `SpringEndpointConfigurator`: Replaced `ApplicationContextAware` with `CDI.current().select()` for endpoint instances
- `MarketSummaryWebSocket`: Standard `@ServerEndpoint` (auto-discovered by Jakarta EE)
- `MarketSummaryWebSocketEvents`: `@EventListener @Async` replaced with `@ObservesAsync`

### Configuration
- `AsyncConfig`: Spring `TaskExecutor`/`TaskScheduler` beans replaced with `@Resource ManagedExecutorService` + `@Produces`
- `DualPortConfig`: Emptied (Liberty handles ports via `server.xml`)
- `JmsConfig`: Emptied (Liberty handles JMS via `server.xml`)
- `WebSocketEndpointConfig`: Emptied (Jakarta EE auto-discovers `@ServerEndpoint`)
- `ApplicationProps`: `@ConfigurationProperties` replaced with `@ApplicationScoped` reading system properties

---

## XML Descriptor Changes

### persistence.xml
- Updated to Jakarta Persistence namespace (`https://jakarta.ee/xml/ns/persistence`)
- Version set to `3.0` (Liberty's JAXB parser does not accept `3.1`)
- Removed duplicate file from `webapp/WEB-INF/classes/`
- Canonical location: `src/main/resources/META-INF/persistence.xml`

### beans.xml
- Updated from CDI 1.1 (`xmlns.jcp.org/xml/ns/javaee`) to CDI 4.0 (`jakarta.ee/xml/ns/jakartaee`)

### web.xml
- Updated from Servlet 4.0 (`xmlns.jcp.org/xml/ns/javaee`) to Servlet 6.0 (`jakarta.ee/xml/ns/jakartaee`)

### ejb-jar.xml
- Updated from EJB 3.2 (`xmlns.jcp.org/xml/ns/javaee`) to EJB 4.0 (`jakarta.ee/xml/ns/jakartaee`)

---

## Key Fixes During Migration

1. **Liberty feature names**: Corrected from Java EE names (`jsp-3.1`, `jpa-3.1`, `ejbLite-4.0`) to Liberty Jakarta EE names (`pages-3.1`, `persistence-3.1`, `enterpriseBeansLite-4.0`)
2. **Feature compatibility**: Replaced `wasJmsClient-2.0` (Java EE 7) with `messagingClient-3.0` (Jakarta EE 10); used `webProfile-10.0` instead of `jakartaee-10.0` umbrella to avoid feature conflicts
3. **persistence.xml version**: Liberty 26.x's `JaxbUnmarshaller` accepts versions 1.0, 2.0, 2.1, 2.2, 3.0, 3.2 but NOT 3.1; set to 3.0
4. **`@Transactional` on EJB**: Removed from `MarketSummarySingleton` (`@Singleton` EJB) since EJBs manage transactions internally
5. **`final` field injection**: Changed `CDIEventProducer` from constructor injection with `final` fields to field injection (Liberty rejects `@Resource` on `final` fields)
6. **Ambiguous `Connection` import**: `TradeDirect.java` had both `java.sql.Connection` and `jakarta.jms.Connection`; resolved by using fully qualified names for JMS
7. **H2 connection string**: Removed `AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE` (unsupported combination in H2 2.x)
8. **CDI `Map<String, TradeServices>` injection**: Created `TradeServicesProducer` with `@Produces` method using CDI `Instance.Handle` API to build the named-bean map that Spring auto-wired

---

## Files Modified (108 Java files + config)

All 108 Java source files were updated to remove Spring imports and annotations. Key structural changes were made to:
- `impl/direct/TradeDirect.java` - JDBC-based trade service
- `impl/ejb3/TradeSLSBBean.java` - EJB-based trade service
- `impl/ejb3/MarketSummarySingleton.java` - Market summary scheduler
- `impl/ejb3/AsyncScheduledOrder*.java` - Async order processing
- `jms/DTBroker3Listener.java` - JMS MDB for order processing
- `jms/DTStreamer3Listener.java` - JMS MDB for market streaming
- `web/servlet/TradeServletAction.java` - Main trade UI controller
- `web/websocket/MarketSummary*.java` - WebSocket market updates
- `config/TradeServicesProducer.java` (NEW) - CDI producer for service map
