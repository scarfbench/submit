# Migration Changelog: Spring Boot to Jakarta EE

## [2026-03-16T22:20:00Z] [info] Project Analysis
- Identified Address Book application built with Spring Boot + JoinFaces (JSF integration)
- 8 Java source files requiring migration across packages: entity, repo, service, web, config
- Spring Boot starters: data-jpa, web, validation, test, tomcat
- JoinFaces jsf-spring-boot-starter for JSF integration
- Jakarta Faces 4.0.2, Weld Servlet Core 5.1.0 already in use
- H2 in-memory database for persistence
- 6 XHTML JSF pages: index, template, Create, Edit, List, View
- WAR packaging with embedded Tomcat via Spring Boot

## [2026-03-16T22:22:00Z] [info] Dependency Migration - pom.xml
- Removed all Spring Boot dependencies:
  - spring-boot-starter-data-jpa
  - spring-boot-starter-web
  - spring-boot-starter-validation
  - spring-boot-starter-test
  - spring-boot-starter-tomcat
  - spring-boot-maven-plugin
- Removed JoinFaces dependencies:
  - joinfaces-platform (BOM)
  - jsf-spring-boot-starter
- Removed standalone Jakarta Faces and Weld dependencies (now provided by app server)
- Removed Spring Core version management
- Added single Jakarta EE 10 API dependency (scope: provided)
- Updated artifact name from address-book-spring to address-book-jakarta
- Added maven-compiler-plugin 3.11.0 and maven-war-plugin 3.4.0
- Set finalName to "address-book" for clean WAR naming

## [2026-03-16T22:24:00Z] [info] Java Source Code Migration

### Application.java
- **Before:** Spring Boot main class with `@SpringBootApplication` and `SpringApplication.run()`
- **After:** JAX-RS application activator with `@ApplicationPath("/api")` extending `jakarta.ws.rs.core.Application`

### ServletInitializer.java
- **Before:** Spring Boot WAR initializer extending `SpringBootServletInitializer`
- **After:** Empty class (no longer needed; Jakarta EE app server manages lifecycle)

### config/JsfConfig.java
- **Before:** Spring `@Configuration` with `@Bean` for JSF `FacesServlet` registration via `ServletRegistrationBean`
- **After:** Empty class (JSF servlet registration handled by web.xml in Jakarta EE)

### config/SpringConfig.java
- **Before:** Spring `@Configuration` with `@ComponentScan`
- **After:** Empty class (CDI bean discovery handled by beans.xml)

### entity/Contact.java
- **Before:** JPA entity with `jakarta.persistence.*` and `jakarta.validation.*` annotations
- **After:** Same JPA/validation annotations (already using jakarta namespace), added `@Table(name = "CONTACT")`
- No import changes needed (entity was already using jakarta.* namespace)

### repo/ContactRepository.java
- **Before:** Spring Data JPA interface extending `JpaRepository<Contact, Long>` with `@Repository`
- **After:** CDI bean with `@ApplicationScoped`, `@PersistenceContext` for EntityManager injection
- Implemented all repository methods manually: save, findById, existsById, deleteById, delete, findAll, findAll(offset, maxResults), count
- Uses JPQL queries for list and count operations

### service/ContactService.java
- **Before:** Spring `@Service` with `@Transactional` (org.springframework.transaction), constructor injection
- **After:** CDI `@ApplicationScoped` with `@Transactional` (jakarta.transaction), `@Inject` for dependency injection
- Added no-arg constructor (required by CDI proxy generation)
- Updated findRange to use repository's offset/maxResults-based pagination instead of Spring Data's PageRequest

### web/ContactController.java
- **Before:** Spring `@Component("contactController")` with `@Scope("session")`, `@Autowired`
- **After:** CDI `@Named("contactController")` with `@SessionScoped`, `@Inject`
- Added no-arg constructor (required by CDI proxy generation)
- Replaced `jakarta.faces.application.FacesMessage` inline usage with proper import
- All JSF-specific code (FacesContext, FacesMessage, ResourceBundle) unchanged

## [2026-03-16T22:25:00Z] [info] REST API Endpoint Added
- Created `rest/ContactResource.java` - JAX-RS resource for CRUD operations
- Endpoints: GET /api/contacts, GET /api/contacts/{id}, POST /api/contacts, PUT /api/contacts/{id}, DELETE /api/contacts/{id}, GET /api/contacts/count, GET /api/contacts/health
- Uses CDI `@Inject` for ContactService dependency

## [2026-03-16T22:26:00Z] [info] Configuration Files

### Added: src/main/resources/META-INF/persistence.xml
- Persistence unit: addressBookPU with JTA transaction type
- Data source: java:jboss/datasources/ExampleDS (WildFly built-in H2 datasource)
- Schema generation: drop-and-create
- Hibernate SQL logging enabled

### Added: src/main/webapp/WEB-INF/beans.xml
- CDI beans.xml version 4.0 with bean-discovery-mode="all"
- Enables CDI bean scanning for all application classes

### Added: src/main/webapp/WEB-INF/web.xml
- Web app descriptor version 6.0
- Configures FacesServlet mapped to *.xhtml
- Sets welcome file to index.xhtml
- Sets Jakarta Faces project stage to Development

### Added: src/main/webapp/WEB-INF/faces-config.xml
- Jakarta Faces config version 4.0
- Configures Bundle resource bundle (same as original)

### Copied to src/main/webapp/
- XHTML pages: index.xhtml, template.xhtml, contact/*.xhtml
- CSS: resources/css/jsfcrud.css
- Standard WAR structure for Jakarta EE deployment

### Modified: src/main/resources/application.properties
- Replaced Spring Boot configuration with comment (no longer used)

## [2026-03-16T22:27:00Z] [info] Dockerfile Migration
- **Before:** Single-stage build using maven:3.9.12-ibm-semeru-21-noble, runs `mvn clean package spring-boot:run`
- **After:** Multi-stage build:
  - Stage 1 (builder): maven:3.9.12-eclipse-temurin-17 for compilation
  - Stage 2 (runtime): eclipse-temurin:17-jdk-noble with WildFly 31.0.1.Final installed manually
- Ubuntu Noble base provides easy Python 3 installation for smoke tests
- WildFly downloaded from GitHub releases and extracted to /opt/jboss/wildfly
- Preserves uv, Playwright, pytest, and requests installation for smoke testing
- WAR file deployed to WildFly's standalone/deployments directory
- CMD changed from Maven spring-boot:run to WildFly standalone.sh

## [2026-03-16T22:28:00Z] [warning] Docker Build Issue - CentOS 7 Base
- Initial attempt to use quay.io/wildfly/wildfly:31.0.1.Final-jdk17 official image
- Failed because CentOS 7 base lacks microdnf and Python 3 support
- Resolution: Switched to Ubuntu Noble (eclipse-temurin:17-jdk-noble) with manual WildFly installation

## [2026-03-16T22:29:00Z] [info] Smoke Tests Created
- Created smoke.py with 11 test cases covering:
  1. Health endpoint (GET /api/contacts/health)
  2. Get all contacts (initially empty)
  3. Create contact (POST /api/contacts)
  4. Count contacts (GET /api/contacts/count)
  5. JSF index page (GET /index.xhtml)
  6. JSF list page (GET /contact/List.xhtml)
  7. JSF create page (GET /contact/Create.xhtml)
  8. Get contact by ID (GET /api/contacts/{id})
  9. Update contact (PUT /api/contacts/{id})
  10. Delete contact (DELETE /api/contacts/{id})
  11. Get non-existent contact returns 404

## [2026-03-16T22:32:00Z] [info] Build & Deployment Success
- Docker image built successfully (multi-stage)
- Maven compilation: 0 errors
- WildFly deployment: address-book.war deployed successfully
- Server startup time: ~6.3 seconds
- All 11 smoke tests: PASSED

## [2026-03-16T22:35:00Z] [info] Migration Complete
- Framework migration: Spring Boot -> Jakarta EE 10 (WildFly 31.0.1)
- All business logic preserved
- JSF pages render correctly
- REST API fully functional
- JPA persistence operational with H2 in-memory database
- CDI dependency injection working throughout the application
