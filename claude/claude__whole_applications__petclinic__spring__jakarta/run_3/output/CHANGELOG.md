# CHANGELOG - Spring Boot to Jakarta EE Migration

## Migration Summary

**Source Framework**: Spring Boot 3.3.0 (Spring MVC, Spring Data JPA, Thymeleaf with Spring integration)
**Target Framework**: Jakarta EE 10 (CDI, JPA, Servlets, JAX-RS, standalone Thymeleaf)
**Runtime**: WildFly 39.0.1.Final (JDK 21)
**Packaging**: Changed from executable JAR (Spring Boot) to WAR deployed to WildFly

## Changes Made

### 1. Project Configuration (pom.xml)
- Removed `spring-boot-starter-parent` parent POM
- Removed all `spring-boot-starter-*` dependencies (web, thymeleaf, data-jpa, test, cache, etc.)
- Removed Spring-specific Maven plugins (spring-boot-maven-plugin)
- Added `jakarta.jakartaee-api:10.0.0` (provided scope - supplied by WildFly)
- Added `org.thymeleaf:thymeleaf:3.1.2.RELEASE` (standalone, without Spring integration)
- Added `com.fasterxml.jackson.core:jackson-databind:2.17.0` for JSON serialization
- Added `com.h2database:h2:2.2.224` for in-memory database
- Changed packaging from JAR to WAR with `finalName=petclinic`
- Added `maven-war-plugin` and `maven-dependency-plugin` for webjar unpacking

### 2. Package Structure Refactoring
- Moved from `org.springframework.samples.petclinic.*` to `org.petclinic.*`
- New package layout:
  - `org.petclinic.model` - JPA entity classes
  - `org.petclinic.repository` - Data access objects using EntityManager
  - `org.petclinic.controller` - Servlet-based controllers
  - `org.petclinic.config` - CDI configuration, DB initialization, JAX-RS

### 3. Dependency Injection
- Replaced Spring `@Autowired` / `@Service` / `@Component` with Jakarta CDI:
  - `@ApplicationScoped` for singleton services
  - `@Inject` for dependency injection
  - `@Produces` for CDI producer methods
  - `@Singleton @Startup` EJB for database initialization

### 4. Web Layer
- Replaced Spring MVC `@Controller` / `@RequestMapping` with Jakarta Servlets:
  - `WelcomeServlet` - `@WebServlet({"", "/"})`
  - `OwnerServlet` - `@WebServlet("/owners/*")` with manual path routing
  - `PetServlet` - `@WebServlet("/pets/*")`
  - `VisitServlet` - `@WebServlet("/visits/*")`
  - `VetServlet` - `@WebServlet({"/vets.html", "/vets"})` with HTML and JSON support
  - `CrashServlet` - `@WebServlet("/oups")`
  - `ErrorServlet` - `@WebServlet("/error")`
- Created `BaseServlet` abstract class with Thymeleaf integration helper methods
- Added `JaxRsApplication` with `@ApplicationPath("/api")` for REST endpoints

### 5. Persistence Layer
- Replaced Spring Data JPA repositories with manual JPA `EntityManager` queries:
  - `OwnerRepository` - JPQL queries for find, search, count, save
  - `PetTypeRepository` - JPQL queries for pet types
  - `VetRepository` - JPQL queries with pagination for vets
- Used `@PersistenceContext` for EntityManager injection
- Used `@Transactional` (Jakarta) for write operations
- Created `persistence.xml` with JTA persistence unit pointing to WildFly datasource

### 6. Model Classes
- Preserved all entity relationships (Owner->Pet->Visit, Vet->Specialty)
- Used Jakarta Persistence annotations (`jakarta.persistence.*`)
- Used Jakarta Validation annotations (`jakarta.validation.*`)
- Key entities: BaseEntity, NamedEntity, Person, Owner, Pet, PetType, Visit, Vet, Specialty

### 7. Template Layer (Thymeleaf)
- Migrated from Spring-integrated Thymeleaf to standalone Thymeleaf 3.1
- Removed Spring-specific features:
  - `th:field` replaced with `th:value` + plain `name` attributes
  - `th:object` form binding removed, using direct variable access
  - `#fields.hasErrors()` / `#fields.errors()` replaced with manual error list
  - `#{message.key}` Spring message expressions replaced with hardcoded text or variables
  - `@{/path(param=value)}` simplified for non-Spring URL generation
- Removed Spring-specific fragment templates (`inputField.html`, `selectField.html`)
- Updated `layout.html` to use hardcoded navigation instead of Spring message expressions

### 8. Configuration Files
- Created `src/main/resources/META-INF/persistence.xml` for JPA configuration
- Created `src/main/webapp/WEB-INF/beans.xml` for CDI (bean-discovery-mode="all")
- Created `src/main/webapp/WEB-INF/web.xml` for servlet and error page configuration
- Created `configure-wildfly.cli` for WildFly H2 datasource configuration
- Removed `application.properties`, `application-mysql.properties`, `application-postgres.properties`

### 9. Database Initialization
- Created `DatabaseInitializer` as `@Singleton @Startup` EJB
- Reads and executes `db/h2/schema.sql` and `db/h2/data.sql` at application startup
- Uses `@Resource` DataSource lookup for `java:jboss/datasources/PetClinicDS`

### 10. Docker Configuration
- Multi-stage Dockerfile:
  - Stage 1: Maven build with `maven:3.9.12-ibm-semeru-21-noble`
  - Stage 2: WildFly runtime with `quay.io/wildfly/wildfly:latest`
- WildFly CLI script configures H2 datasource at image build time
- WAR deployed to WildFly deployments directory as `ROOT.war`
- Python 3 and `requests` library installed for smoke testing

### 11. Files Removed
- All Spring source files under `src/main/java/org/springframework/`
- All test files under `src/test/`
- Spring configuration: `application.properties`, `application-mysql.properties`, `application-postgres.properties`
- Spring banner: `banner.txt`
- Checkstyle configuration directory

### 12. Smoke Tests (smoke.py)
- Created HTTP-based smoke tests using Python `requests` library
- 9 test cases covering:
  1. Welcome page loads
  2. Find owners page loads
  3. Owners list loads with data
  4. Owner details page loads
  5. Create owner functionality
  6. Vets HTML page with pagination
  7. Vets JSON API endpoint
  8. Owner search functionality
  9. Error page handling

## Errors Encountered and Resolutions

### Error 1: WildFly Docker Image Tag Not Found
- **Error**: `quay.io/wildfly/wildfly:31.0.1.Final-jdk21` image not found
- **Resolution**: Changed to `quay.io/wildfly/wildfly:latest` (resolved to 39.0.1.Final with JDK 21)

### Error 2: DNS Resolution in Docker Build
- **Error**: Maven could not resolve `repo.maven.apache.org` during Docker build
- **Resolution**: Added `--network=host` to `docker build` command

### Error 3: Package Conflict in WildFly Image
- **Error**: `curl` package conflicts with `curl-minimal` in WildFly UBI image
- **Resolution**: Removed `curl` from package installation (curl-minimal is sufficient)

### Error 4: Playwright Incompatible with UBI
- **Error**: Playwright requires `apt-get` but WildFly image is RHEL/UBI-based
- **Resolution**: Removed Playwright; smoke tests use `requests` (HTTP-based, not browser-based)

### Error 5: Duplicate H2 JDBC Driver in WildFly
- **Error**: `WFLYCTL0212: Duplicate resource` when adding H2 JDBC driver
- **Resolution**: WildFly already ships with H2 driver; removed driver registration from CLI script

### Error 6: CDI Proxy Error for TemplateEngine
- **Error**: `WELD-001480: Bean type class org.thymeleaf.TemplateEngine is not proxyable because it contains a final method`
- **Resolution**: Changed CDI producer to return `ITemplateEngine` interface instead of concrete `TemplateEngine` class; updated injection point in `BaseServlet`

## Test Results

All 9 smoke tests passed:
```
PASS: Welcome page loads successfully
PASS: Find owners page loads successfully
PASS: Owners list loads with data
PASS: Owner details page loads successfully
PASS: Create owner works successfully
PASS: Vets HTML page loads successfully
PASS: Vets JSON/XML API works successfully
PASS: Owner search works successfully
PASS: Error page works as expected

Results: 9 passed, 0 failed out of 9 tests
```
