# CHANGELOG - Quarkus to Jakarta EE Migration

## Migration Summary

Migrated the Order Java Persistence application from **Quarkus** to **Jakarta EE 10** running on **Open Liberty**.

### Source Framework
- Quarkus with Qute templating, Hibernate ORM (Panache-style), RESTEasy Reactive
- H2 in-memory database
- Uber-jar packaging

### Target Framework
- Jakarta EE 10 on Open Liberty 26.x
- Thymeleaf 3.1 templating (replacing Qute)
- EclipseLink JPA (Liberty's default provider)
- JAX-RS (RESTEasy via Liberty)
- CDI 4.0 for dependency injection
- WAR packaging deployed to Open Liberty

---

## Changes Made

### 1. Build Configuration (pom.xml)
- Removed all Quarkus BOM and plugin references (`quarkus-maven-plugin`, `quarkus-bom`)
- Removed Quarkus-specific dependencies: `quarkus-hibernate-orm`, `quarkus-resteasy-reactive`, `quarkus-qute`, `quarkus-jdbc-h2`
- Added `jakarta.jakartaee-api:10.0.0` (provided scope) for Jakarta EE 10 APIs
- Added `org.thymeleaf:thymeleaf:3.1.2.RELEASE` for templating
- Added `com.h2database:h2:2.2.224` for database driver
- Added `org.slf4j:slf4j-simple:2.0.9` for Thymeleaf logging
- Changed packaging from `jar` to `war`
- Added `maven-war-plugin:3.4.0` and `liberty-maven-plugin:3.10.3`
- Changed compiler target to Java 17

### 2. Application Server Configuration
- **Created** `src/main/liberty/config/server.xml` - Open Liberty server configuration
  - Feature: `webProfile-10.0` (includes CDI, JPA, JAX-RS, Servlet, EJB Lite, etc.)
  - H2 JDBC library and DataSource (`jdbc/orderDS`) with in-memory URL
  - HTTP endpoint on port 8080
  - Web application deployment configuration with H2 classloader reference

### 3. JPA Configuration
- **Created** `src/main/resources/META-INF/persistence.xml`
  - Persistence unit `orderPU` with JTA transaction type
  - JNDI datasource reference `jdbc/orderDS`
  - All 5 entity classes explicitly listed
  - Schema generation configured for `create` mode
  - EclipseLink DDL generation properties

### 4. CDI and Web Configuration
- **Created** `src/main/webapp/WEB-INF/beans.xml` with `bean-discovery-mode="all"`
- **Created** `src/main/webapp/WEB-INF/web.xml` (Jakarta EE 6.0 web descriptor)

### 5. Package Rename
- Renamed all Java packages from `quarkus.tutorial.order.*` to `jakarta.tutorial.order.*`
- Updated all internal imports accordingly

### 6. Entity Classes (Minimal Changes)
- `CustomerOrder`, `LineItem`, `LineItemKey`, `Part`, `PartKey`, `Vendor`, `VendorPart`
- Only package name changed; JPA annotations and logic preserved

### 7. Repository Classes
- All 5 repositories: `CustomerOrderRepository`, `LineItemRepository`, `PartRepository`, `VendorPartRepository`, `VendorRepository`
- Added `unitName="orderPU"` to all `@PersistenceContext` annotations
- Replaced Quarkus-style `@Transactional` with Jakarta `@Transactional`
- Changed scope from Quarkus default to `@ApplicationScoped`

### 8. Service Layer
- `OrderConfigService` - Package rename, removed `@Transactional` (transaction managed by caller)

### 9. Web Layer
- **Created** `OrderApplication.java` - JAX-RS `@ApplicationPath("/")` application class
- **Created** `ThymeleafEngine.java` - CDI `@ApplicationScoped` bean wrapping Thymeleaf `TemplateEngine` with `ClassLoaderTemplateResolver`
- **Rewritten** `OrderWebController.java`:
  - Converted from Qute `TemplateInstance` returns to Thymeleaf `String` (HTML) returns
  - Added entity-to-Map conversion methods to avoid OGNL restricted access issues in Thymeleaf 3.1
  - All `@GET`/`@POST` JAX-RS endpoints preserved with same URL paths
- **Rewritten** `StartupInitializer.java`:
  - Changed from Quarkus `@Observes StartupEvent` to EJB `@Singleton @Startup` with `@TransactionManagement(BEAN)`
  - Uses `UserTransaction` for manual transaction control during data initialization
- **Created** `StaticResourceController.java` - JAX-RS endpoint for serving CSS files (since `@ApplicationPath("/")` intercepts static resources)
  - Includes path traversal protection (rejects `..`, `/`, `\` in filenames)

### 10. Template Migration (Qute to Thymeleaf)
- **Converted** `orders.html`:
  - Replaced Qute `{#each}` with Thymeleaf `th:each`
  - Replaced Qute `{order.orderId}` with `th:text="${order.orderId}"`
  - Replaced Qute `{#if}` with `th:if`
  - Used Thymeleaf string concatenation for link URLs instead of `@{...}` (non-web context)
- **Converted** `lineItems.html`:
  - Same Qute-to-Thymeleaf conversions
  - Simplified vendor part number access (pre-resolved in controller)
- **Removed** `orderTemplate.html` and `lineItemTemplate.html` (Qute include templates, not needed with Thymeleaf)

### 11. Static Resources
- Copied `css/default.css` to `src/main/resources/css/` for ClassLoader-based serving
- Retained copy in `src/main/webapp/css/` as fallback

### 12. Dockerfile
- Changed from single-stage Quarkus build to multi-stage build:
  - Stage 1: Maven build (`maven:3.9.12-ibm-semeru-21-noble`) with Playwright for smoke tests
  - Stage 2: Open Liberty runtime (`icr.io/appcafe/open-liberty:full-java17-openj9-ubi`)
- Copies server.xml, H2 JAR, WAR file, and smoke test tooling into runtime image
- Proper user permissions (root for COPY, user 1001 for runtime)
- Runs `configure.sh` for Liberty feature pre-installation

### 13. Removed Files
- `src/main/java/quarkus/` (entire old package tree)
- `src/main/docker/` (Quarkus-specific Docker files)
- `src/main/resources/application.properties` (Quarkus configuration)
- Qute template includes (`orderTemplate.html`, `lineItemTemplate.html`)

### 14. Smoke Tests (smoke.py)
- Created 14 pytest-based smoke tests using aiohttp:
  - Orders page: loads, contains preloaded orders (1111, 4312), shipment info, create form, vendor search
  - Line items page: loads, shows items, shows parts table
  - Create order (9999), remove order (8888)
  - Find vendor (WidgetCorp, Gadget)
  - Add line item to order
  - CSS static resource loads

---

## Errors Encountered and Resolutions

### Error 1: `configure.sh` Permission Denied
- **Cause**: Open Liberty image runs as non-root user 1001; COPY commands didn't set proper ownership
- **Fix**: Added `USER root` before COPY, used `--chown=1001:0`, switched to `USER 1001` before `configure.sh`

### Error 2: H2 DataSource URL Not Passed
- **Cause**: Liberty's `<properties.generic>` element didn't correctly pass the JDBC URL to H2
- **Fix**: Used plain `<properties url="..." user="sa" password=""/>` element with `type="javax.sql.DataSource"`

### Error 3: `${default.http.port}` Not Resolved
- **Cause**: Liberty Maven plugin property not available in Docker runtime
- **Fix**: Hardcoded `httpPort="8080"` in server.xml

### Error 4: Startup Data Initialization Not Firing
- **Cause**: `@WebListener` with `@Inject` didn't trigger in Liberty's CDI/Servlet integration
- **Fix**: Changed to EJB `@Singleton @Startup` with Bean-Managed Transactions

### Error 5: Thymeleaf `@{...}` Link Expression Failure
- **Cause**: `@{/path}` requires `IWebContext` (web context); we used standalone `Context`
- **Fix**: Replaced `@{...}` with string concatenation: `'/lineItems?orderId=' + ${order.orderId}`

### Error 6: Thymeleaf OGNL Restricted Member Access
- **Cause**: Thymeleaf 3.1 restricts OGNL property access on non-whitelisted classes
- **Fix**: Converted JPA entities to `Map<String, Object>` in the controller before passing to templates

---

## Validation Results

All 14 smoke tests passed:

```
smoke.py::TestOrdersPage::test_orders_page_loads PASSED
smoke.py::TestOrdersPage::test_orders_page_contains_preloaded_orders PASSED
smoke.py::TestOrdersPage::test_orders_page_contains_shipment_info PASSED
smoke.py::TestOrdersPage::test_orders_page_has_create_form PASSED
smoke.py::TestOrdersPage::test_orders_page_has_vendor_search PASSED
smoke.py::TestLineItemsPage::test_line_items_page_loads PASSED
smoke.py::TestLineItemsPage::test_line_items_shows_items PASSED
smoke.py::TestLineItemsPage::test_line_items_shows_parts_table PASSED
smoke.py::TestCreateOrder::test_create_order PASSED
smoke.py::TestRemoveOrder::test_remove_order PASSED
smoke.py::TestFindVendor::test_find_vendor PASSED
smoke.py::TestFindVendor::test_find_vendor_gadget PASSED
smoke.py::TestAddLineItem::test_add_line_item PASSED
smoke.py::TestStaticResources::test_css_loads PASSED

14 passed in 0.67s
```
