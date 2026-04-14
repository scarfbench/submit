# Migration Changelog: Quarkus to Jakarta EE

## [2026-03-17T03:30:00Z] [info] Project Analysis
- Identified 16 Java source files in package `quarkus.tutorial.order`
- Detected Quarkus 3.30.5 in pom.xml with dependencies: quarkus-arc, quarkus-rest, quarkus-hibernate-orm, quarkus-hibernate-orm-panache, quarkus-jdbc-h2, quarkus-hibernate-validator, quarkus-qute, quarkus-rest-qute, quarkus-undertow
- Entities: CustomerOrder, LineItem, Part, Vendor, VendorPart (plus composite keys LineItemKey, PartKey)
- Repositories: CustomerOrderRepository, LineItemRepository, PartRepository, VendorRepository, VendorPartRepository
- Web controllers: OrderWebController (Qute-templated), OrderController
- Service: OrderConfigService (data initialization)
- StartupInitializer using Quarkus-specific `@Observes StartupEvent`
- Templates: 4 Qute HTML templates (orders.html, orderTemplate.html, lineItems.html, lineItemTemplate.html)
- Configuration: application.properties with Quarkus-specific keys (quarkus.http.port, quarkus.datasource.*, quarkus.hibernate-orm.*, quarkus.qute.*)
- Dockerfile: Maven-based with `mvn quarkus:run` CMD

## [2026-03-17T03:31:00Z] [info] Smoke Tests Generated
- Created smoke.py with 6 test cases covering: orders page, line items page, order creation, vendor search, order removal, line item addition
- Tests use Python requests library for HTTP interactions
- Tests validate both HTTP status codes and response body content

## [2026-03-17T03:32:00Z] [info] Dependency Migration (pom.xml)
- Changed groupId from `quarkus.tutorial` to `jakarta.tutorial`
- Changed artifactId from `order-quarkus` to `order-jakarta`
- Changed packaging to `war` (from implicit jar)
- Removed Quarkus BOM dependency management
- Removed all `io.quarkus` dependencies (quarkus-arc, quarkus-rest, quarkus-hibernate-orm, quarkus-hibernate-orm-panache, quarkus-jdbc-h2, quarkus-hibernate-validator, quarkus-qute, quarkus-rest-qute, quarkus-undertow)
- Removed test dependencies (quarkus-junit5, rest-assured)
- Added `jakarta.jakartaee-api:10.0.0` (scope: provided)
- Added `com.h2database:h2:2.2.224` (runtime)
- Replaced quarkus-maven-plugin with liberty-maven-plugin
- Removed native profile and Quarkus-specific surefire/failsafe configuration
- Added maven-war-plugin 3.4.0

## [2026-03-17T03:33:00Z] [info] Jakarta EE Configuration Files Created
- Created `src/main/liberty/config/server.xml`: Open Liberty server configuration with webProfile-10.0 and enterpriseBeansLite-4.0 features, H2 datasource, web application deployment
- Created `src/main/resources/META-INF/persistence.xml`: JPA persistence unit "order-pu" with JTA transaction type, H2 datasource JNDI binding, EclipseLink DDL generation
- Created `src/main/webapp/WEB-INF/web.xml`: Jakarta EE 6.0 web application descriptor
- Created `src/main/webapp/WEB-INF/beans.xml`: CDI 4.0 beans descriptor with bean-discovery-mode="all"
- Created `src/main/webapp/index.html`: Redirect to /orders
- Copied `css/default.css` to `src/main/webapp/css/`

## [2026-03-17T03:34:00Z] [info] Package Refactoring
- Renamed all Java packages from `quarkus.tutorial.order` to `jakarta.tutorial.order`
- Created new directory structure: `src/main/java/jakarta/tutorial/order/{entity,repository,service,web}`
- All 16 Java source files migrated to new package

## [2026-03-17T03:35:00Z] [info] Code Refactoring - Entities
- All 7 entity/key classes (CustomerOrder, LineItem, LineItemKey, Part, PartKey, Vendor, VendorPart) already used `jakarta.persistence.*` imports - only package declaration changed
- No annotation changes needed - JPA annotations are standard Jakarta EE

## [2026-03-17T03:35:30Z] [info] Code Refactoring - Repositories
- All 5 repository classes already used `jakarta.enterprise.context.ApplicationScoped`, `jakarta.persistence.EntityManager`, `jakarta.persistence.PersistenceContext`, `jakarta.transaction.Transactional`
- Added `unitName = "order-pu"` to all `@PersistenceContext` annotations for explicit persistence unit binding
- Only package declaration and import paths changed

## [2026-03-17T03:36:00Z] [info] Code Refactoring - StartupInitializer (Quarkus-specific)
- Replaced Quarkus-specific `io.quarkus.runtime.StartupEvent` with Jakarta EE `@Singleton @Startup` EJB pattern
- Replaced `@ApplicationScoped` + `@Observes StartupEvent` with `@Singleton @Startup @PostConstruct`
- Removed `import io.quarkus.runtime.StartupEvent`
- Added `import jakarta.annotation.PostConstruct`, `import jakarta.ejb.Singleton`, `import jakarta.ejb.Startup`

## [2026-03-17T03:36:30Z] [info] Code Refactoring - OrderWebController (Quarkus-specific)
- Removed Qute template dependency: `io.quarkus.qute.Template`, `io.quarkus.qute.TemplateInstance`
- Replaced Qute `@Inject Template` fields and `TemplateInstance` return types with `String` return type
- Implemented HTML string builder methods: `buildOrdersPage()` and `buildLineItemsPage()`
- Added `escapeHtml()` utility method for XSS prevention
- Preserved all original HTML structure, form actions, and data display
- All 6 REST endpoints maintained: GET /orders, POST /submitOrder, POST /removeOrder, GET /lineItems, POST /addLineItem, POST /findVendor

## [2026-03-17T03:37:00Z] [info] JAX-RS Application Class Created
- Created `JakartaRestApplication.java` extending `jakarta.ws.rs.core.Application` with `@ApplicationPath("/")`
- Required for JAX-RS endpoint discovery in Jakarta EE application servers

## [2026-03-17T03:37:30Z] [info] Old Quarkus Files Removed
- Removed entire `src/main/java/quarkus/` directory tree
- Removed `src/main/resources/application.properties` (Quarkus-specific config)
- Removed `src/main/resources/templates/` directory (Qute templates: orders.html, orderTemplate.html, lineItems.html, lineItemTemplate.html)
- Removed `src/main/resources/css/` (moved to webapp)

## [2026-03-17T03:38:00Z] [info] Dockerfile Updated
- Changed to multi-stage build: Maven builder stage + Open Liberty runtime stage
- Builder stage: `maven:3.9.12-ibm-semeru-21-noble` for compilation
- Runtime stage: `icr.io/appcafe/open-liberty:full-java17-openj9-ubi`
- Replaced `mvn quarkus:run` CMD with Open Liberty `server run defaultServer`
- Added H2 JDBC driver copy to Liberty lib directory
- Added server.xml copy to /config/
- Added WAR deployment to /config/apps/
- Installed python3 and requests for smoke testing
- UBI image uses `dnf` instead of `apt-get`

## [2026-03-17T03:39:00Z] [error] First Build Attempt - Docker Runtime Error
- Error: `apt-get: command not found` in Open Liberty UBI image
- Root Cause: UBI (Universal Base Image) uses `dnf`/`yum`, not `apt-get`
- Resolution: Changed `apt-get` to `dnf install -y python3 python3-pip curl lsof`

## [2026-03-17T03:40:00Z] [error] Second Build Attempt - H2 DataSource Error
- Error: `URL format error; must be "jdbc:h2:..."` and `DSRA8100E: Unable to get a XAConnection from the DataSource`
- Root Cause: `properties.generic` element in server.xml didn't correctly pass the JDBC URL to H2
- Additional Issue: `jakartaee-10.0` feature included full EJB/JMS/ORB which required user registry configuration
- Resolution: Changed `properties.generic` to `properties` element; Changed features from `jakartaee-10.0` to `webProfile-10.0` + `enterpriseBeansLite-4.0`; Changed datasource type to `javax.sql.DataSource`

## [2026-03-17T03:41:00Z] [info] Successful Build and Deployment
- Docker image built successfully with no-cache rebuild
- Open Liberty server started in 3.956 seconds
- All entities created successfully (Parts, Vendors, VendorParts, Orders, LineItems)
- Data initialization completed via `@Singleton @Startup` EJB
- Application available at dynamic port

## [2026-03-17T03:42:00Z] [info] Smoke Tests - All Passed
- test_orders_page: PASS - Orders page loads with seeded data (order IDs 1111, 4312)
- test_line_items_page: PASS - Line items page loads for order 1111
- test_submit_order: PASS - New order 9999 created successfully
- test_find_vendor: PASS - Vendor search returns WidgetCorp
- test_remove_order: PASS - Order 9999 removed successfully
- test_add_line_item: PASS - Line item added to order 4312
- All 6 tests passed (0 failures)
- Tests verified both from host machine (port 34443) and inside container (port 8080)

## [2026-03-17T03:43:00Z] [info] Migration Complete
- Framework: Quarkus 3.30.5 -> Jakarta EE 10 (Open Liberty)
- All business logic preserved
- All REST endpoints functional
- All JPA entities and relationships working
- Data initialization working via @Singleton @Startup
- Docker containerization working
- All smoke tests passing
