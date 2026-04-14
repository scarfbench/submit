# CHANGELOG - Spring Boot to Jakarta EE Migration

## Migration: Spring Boot 3.3.7 -> Jakarta EE 10 (Open Liberty)

### Summary
Migrated the Order application from Spring Boot 3.3.7 to Jakarta EE 10 running on Open Liberty. The application manages customer orders, parts, vendors, and line items with JPA persistence, EJB services, JSF/PrimeFaces web UI, and JAX-RS REST endpoints.

### Runtime
- **Before**: Spring Boot 3.3.7 (embedded Tomcat), executable JAR
- **After**: Open Liberty (full-java17-openj9-ubi), WAR deployment

---

### Files Changed

#### Build & Configuration

| File | Action | Description |
|------|--------|-------------|
| `pom.xml` | Modified | Replaced Spring Boot parent/starters with Jakarta EE 10 API (provided scope), H2, PrimeFaces (jakarta classifier). Changed packaging from JAR to WAR. Added maven-war-plugin, liberty-maven-plugin. |
| `Dockerfile` | Modified | Changed from Spring Boot JAR execution to multi-stage build: Maven builder + Open Liberty runtime (`icr.io/appcafe/open-liberty:full-java17-openj9-ubi`). Copies server.xml, H2 JAR, WAR. Uses `yum` for Python/pip install. |
| `src/main/liberty/config/server.xml` | Created | Open Liberty server config: jakartaee-10.0 feature, H2 datasource (JNDI `jdbc/orderDB`), web application deployment, basic user registry, logging. |
| `src/main/resources/META-INF/persistence.xml` | Created | JPA persistence unit `order-pu` with JTA transaction type, `jdbc/orderDB` datasource, drop-and-create schema generation. Lists all 5 entity classes. |
| `src/main/webapp/WEB-INF/beans.xml` | Created | CDI beans descriptor with `bean-discovery-mode="all"`. |
| `src/main/webapp/WEB-INF/web.xml` | Created | Servlet config: FacesServlet mapped to `*.xhtml`, PrimeFaces theme, welcome file. |
| `src/main/webapp/WEB-INF/faces-config.xml` | Created | JSF 4.0 configuration descriptor. |
| `src/main/resources/application.properties` | Deleted | Spring Boot-specific configuration no longer needed. |

#### Source Code

| File | Action | Description |
|------|--------|-------------|
| `OrderApplication.java` | Modified | Replaced `@SpringBootApplication` + `main()` with `@ApplicationPath("/api")` extending `jakarta.ws.rs.core.Application`. |
| `CustomerOrderRepository.java` | Modified | Converted from Spring Data JPA interface to `@Stateless` EJB class with `@PersistenceContext` EntityManager. Implements `findAllOrders()`, `findById()`, `existsById()`, `save()`, `deleteById()`. |
| `LineItemRepository.java` | Modified | Converted from Spring Data JPA interface to `@Stateless` EJB with EntityManager. Implements `findAllLineItems()`, `findLineItemsByOrderId()`, `deleteAllByOrderId()`, `save()`. |
| `PartRepository.java` | Modified | Converted from Spring Data JPA interface to `@Stateless` EJB with EntityManager. Uses `PartKey` composite key for lookups. |
| `VendorRepository.java` | Modified | Converted from Spring Data JPA interface to `@Stateless` EJB with EntityManager. Implements `findById()`, `findVendorsByPartialName()`, `findVendorByCustomerOrder()`, `save()`. |
| `VendorPartRepository.java` | Modified | Converted from Spring Data JPA interface to `@Stateless` EJB with EntityManager. Implements `findAverageVendorPartPrice()`, `findTotalVendorPartPricePerVendor()`, `save()`. |
| `OrderService.java` | Modified | Replaced `@Service @Transactional` (Spring) with `@Stateless` EJB. Changed `@Autowired` to `@EJB` for all 5 repository injections. Business logic unchanged. |
| `DataInitializationService.java` | Modified | Replaced `@Component` + `@EventListener(ApplicationReadyEvent.class)` with `@Singleton @Startup` + `@PostConstruct`. Uses Bean-Managed Transactions (`@TransactionManagement(BEAN)`) with `UserTransaction` for error-safe initialization. |
| `OrderManager.java` | Modified | Changed `@Inject` to `@EJB` for OrderService injection. JSF managed bean logic unchanged. |
| `OrderResource.java` | Created | JAX-RS REST resource at `/orders` with endpoints: `GET /` (all orders), `GET /{orderId}`, `GET /{orderId}/lineitems`, `GET /parts`, `GET /vendors/search?name=`, `GET /health`. Returns JSON via `jakarta.json.Json`. |
| `smoke.py` | Created | Python smoke test suite (8 tests) using `requests` library. Tests: health, orders listing, order detail, line items, parts, vendor search, 404 handling, JSF page accessibility. |

#### Entity Files (Unchanged)
- `CustomerOrder.java` - Already used `jakarta.persistence.*`
- `LineItem.java` - Already used `jakarta.persistence.*`
- `LineItemKey.java` - Already used `jakarta.persistence.*`
- `Part.java` - Already used `jakarta.persistence.*`
- `PartKey.java` - Already used `jakarta.persistence.*`
- `Vendor.java` - Already used `jakarta.persistence.*`
- `VendorPart.java` - Already used `jakarta.persistence.*`

#### Static Resources (Moved)
- XHTML files and CSS moved from `src/main/resources/META-INF/resources/` to `src/main/webapp/` for standard WAR deployment

---

### Annotation Mapping

| Spring Annotation | Jakarta EE Equivalent | Notes |
|---|---|---|
| `@SpringBootApplication` | `@ApplicationPath` + `Application` | JAX-RS application entry point |
| `@Service` | `@Stateless` | EJB session bean |
| `@Repository` (Spring Data interface) | `@Stateless` + `EntityManager` | Manual JPQL implementations |
| `@Component` | `@Singleton` / `@Named` | Depends on use case |
| `@Autowired` | `@EJB` | For EJB references |
| `@Transactional` | Container-Managed Transactions (implicit in `@Stateless`) | CMT is default for EJBs |
| `@EventListener(ApplicationReadyEvent.class)` | `@PostConstruct` in `@Singleton @Startup` | App startup hook |
| Spring Data JPA `JpaRepository` | `EntityManager` with JPQL | Manual query implementation |

---

### Errors Encountered and Resolutions

1. **Dockerfile `apt-get` not found**
   - **Cause**: Open Liberty UBI image is RHEL-based (not Debian/Ubuntu)
   - **Resolution**: Used `yum` package manager instead of `apt-get`

2. **EntityManager null in CDI beans**
   - **Cause**: `@ApplicationScoped` CDI beans don't receive `@PersistenceContext` injection in Liberty
   - **Error**: `NullPointerException: cannot invoke EntityManager.find() because this.em is null`
   - **Resolution**: Changed all 5 repositories from `@ApplicationScoped` CDI beans to `@Stateless` EJBs, which properly support `@PersistenceContext`

3. **Singleton @PostConstruct transaction rollback**
   - **Cause**: `@Singleton @Startup` with Container-Managed Transactions rolls back on RuntimeException, destroying the singleton and preventing app startup
   - **Resolution**: Switched `DataInitializationService` to Bean-Managed Transactions (`@TransactionManagement(BEAN)`) with explicit `UserTransaction` begin/commit/rollback

4. **H2 DataSource URL format error**
   - **Cause**: Liberty's `<properties.h2.embedded>` element produced invalid JDBC URL
   - **Error**: `URL format error; must be "jdbc:h2:..."`
   - **Resolution**: Used generic `<properties URL="jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1" .../>` in server.xml

5. **ORB user registry requirement blocking app startup (10s delay per attempt)**
   - **Cause**: Full Jakarta EE 10 feature set includes EJB remote/ORB which requires a user registry
   - **Error**: `CWWKS9660E: The orb element requires a user registry but no user registry became available within 10 seconds`
   - **Resolution**: Added `<basicRegistry id="basic" realm="BasicRealm"/>` to server.xml. App startup improved from ~50s to ~12s.

6. **Injection type mismatch (`@Inject` vs `@EJB`)**
   - **Cause**: After converting services/repos to `@Stateless` EJBs, CDI `@Inject` didn't properly resolve EJB references
   - **Resolution**: Changed `@Inject` to `@EJB` in `OrderManager`, `OrderResource`, `DataInitializationService`, and `OrderService`

---

### Smoke Test Results

All 8 tests passed:
```
PASS: health endpoint
PASS: get orders (2 orders found)
PASS: get order detail (order 1111)
PASS: get line items for order 1111 (3 items)
PASS: get parts (5 parts found)
PASS: search vendors (1 found)
PASS: order not found returns 404
PASS: JSF page accessible

Results: 8 passed, 0 failed out of 8
```

### Docker Details
- **Image**: `my_test_image_1` (multi-stage: Maven 3.9.12 builder + Open Liberty full-java17-openj9-ubi)
- **Container**: `my_test_container_1` with dynamic port mapping (`-p 0:8080`)
- **Startup time**: ~12 seconds (Liberty) + ~6 seconds (application)
- **Runtime**: Open Liberty with Jakarta EE 10 features
