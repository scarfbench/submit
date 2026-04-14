# Migration Changelog: Quarkus to Jakarta EE

## [2026-03-17T03:12:00Z] [info] Project Analysis
- Identified 6 Java source files in package `quarkus.tutorial.addressbook`
- Detected Quarkus 3.30.5 platform with extensions: quarkus-arc, quarkus-rest, quarkus-hibernate-orm, quarkus-hibernate-validator, quarkus-jdbc-h2, myfaces-quarkus, quarkus-undertow
- Application type: JSF + JPA + CDI Address Book with H2 in-memory database
- Web resources located in Quarkus-specific path: `src/main/resources/META-INF/resources/`
- Build system: Maven with Quarkus Maven Plugin
- Dockerfile using `mvn quarkus:run` for startup

## [2026-03-17T03:13:00Z] [info] Smoke Test Generation
- Created `smoke.py` with 5 smoke tests covering:
  - Index page accessibility
  - Contact list page accessibility
  - Contact creation page accessibility
  - CSS resource serving
  - Form field presence verification
- Tests use Python `requests` library with configurable `BASE_URL`

## [2026-03-17T03:14:00Z] [info] Dependency Migration (pom.xml)
- Removed Quarkus BOM dependency management (`io.quarkus.platform:quarkus-bom:3.30.5`)
- Removed all Quarkus dependencies: quarkus-arc, quarkus-rest, quarkus-hibernate-orm, quarkus-hibernate-validator, quarkus-jdbc-h2, myfaces-quarkus, quarkus-undertow, quarkus-junit5
- Removed rest-assured test dependency
- Added `jakarta.platform:jakarta.jakartaee-api:10.0.0` (scope: provided)
- Changed packaging from `jar` (implicit) to `war`
- Changed groupId from `quarkus.tutorial` to `jakarta.tutorial`
- Changed artifactId from `address-book-quarkus` to `address-book-jakarta`
- Set `<finalName>address-book</finalName>` for consistent WAR naming
- Replaced Quarkus Maven Plugin with maven-war-plugin 3.4.0
- Removed Quarkus-specific surefire/failsafe configurations
- Removed native profile

## [2026-03-17T03:15:00Z] [info] Project Structure Reorganization
- Moved web resources from Quarkus layout (`src/main/resources/META-INF/resources/`) to standard WAR layout (`src/main/webapp/`)
- Files moved:
  - `WEB-INF/web.xml` -> `src/main/webapp/WEB-INF/web.xml`
  - `WEB-INF/faces-config.xml` -> `src/main/webapp/WEB-INF/faces-config.xml`
  - `index.xhtml` -> `src/main/webapp/index.xhtml`
  - `template.xhtml` -> `src/main/webapp/template.xhtml`
  - `contact/*.xhtml` -> `src/main/webapp/contact/*.xhtml`
  - `resources/css/jsfcrud.css` -> `src/main/webapp/resources/css/jsfcrud.css`
- Removed old Quarkus resource directories

## [2026-03-17T03:16:00Z] [info] Java Package Refactoring
- Renamed package from `quarkus.tutorial.addressbook` to `jakarta.tutorial.addressbook`
- Created new directory structure: `src/main/java/jakarta/tutorial/addressbook/`
- Updated all Java files (6 total) with new package declarations and imports:
  - `entity/Contact.java`: Package rename only (already used jakarta.persistence)
  - `ejb/AbstractFacade.java`: Package rename only (already used jakarta.persistence)
  - `ejb/ContactFacade.java`:
    - Package rename
    - Replaced `@ApplicationScoped` + `@Transactional` with `@Stateless` (standard EJB)
    - Replaced `@Inject EntityManager` with `@PersistenceContext(unitName="addressbookPU") EntityManager`
  - `web/ContactController.java`: Package rename and updated internal imports
  - `web/util/JsfUtil.java`: Package rename only
  - `web/util/PaginationHelper.java`: Package rename only
- Removed old `src/main/java/quarkus/` directory tree

## [2026-03-17T03:17:00Z] [info] Jakarta EE Configuration Files
- Created `src/main/resources/META-INF/persistence.xml`:
  - Persistence unit: `addressbookPU` with JTA transaction type
  - Datasource JNDI: `java:jboss/datasources/AddressBookDS`
  - Entity class: `jakarta.tutorial.addressbook.entity.Contact`
  - Schema generation: `create` mode
  - Hibernate SQL logging enabled
- Created `src/main/webapp/WEB-INF/beans.xml`:
  - CDI beans configuration with `bean-discovery-mode="all"`
  - Jakarta CDI 4.0 schema
- Removed `src/main/resources/application.properties` (Quarkus-specific)
- Removed stale `src/main/resources/META-INF/web.xml`
- Removed stale files: `src/main/resources/mvnw`, `src/main/resources/mvnw.cmd`, `src/main/resources/.mvn/`

## [2026-03-17T03:17:30Z] [info] WildFly Datasource Configuration
- Created `configure-ds.cli` WildFly CLI script:
  - Uses embedded server mode for offline configuration
  - Adds H2 datasource `AddressBookDS` at JNDI `java:jboss/datasources/AddressBookDS`
  - Connection URL: `jdbc:h2:mem:addressbook;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
  - Username: `sa`, Password: `sa` (non-empty required by WildFly validation)

## [2026-03-17T03:18:00Z] [info] Dockerfile Migration
- Changed from single-stage Quarkus build to multi-stage build:
  - Stage 1 (build): Maven build producing WAR artifact
  - Stage 2 (runtime): WildFly application server
- Build stage: `maven:3.9.12-ibm-semeru-21-noble` (unchanged)
- Runtime stage: `quay.io/wildfly/wildfly:latest` (WildFly 39.0.1.Final, JDK 21)
- Installed Python 3 and requests library for smoke tests
- WAR deployed to `/opt/jboss/wildfly/standalone/deployments/`
- CLI script runs at build time to configure datasource
- Fixed permissions: `chown -R jboss:jboss /opt/jboss/wildfly/standalone`
- Changed CMD from `["mvn", "quarkus:run"]` to `["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]`

## [2026-03-17T03:18:30Z] [error] Docker Build Failure - WildFly Image Tag
- Error: `quay.io/wildfly/wildfly:31.0.1.Final-jdk21: not found`
- Resolution: Changed to `quay.io/wildfly/wildfly:latest` (resolves to 39.0.1.Final with JDK 21)

## [2026-03-17T03:19:00Z] [error] Docker Build Failure - curl Conflict
- Error: `curl-minimal conflicts with curl` in WildFly base image
- Resolution: Removed `curl` from microdnf install (curl-minimal already present)

## [2026-03-17T03:19:30Z] [error] Docker Build Failure - Empty Password
- Error: WildFly CLI rejected empty password: `WFLYCTL0113: '' is an invalid value for parameter password`
- Resolution: Changed datasource password from empty string to `sa`

## [2026-03-17T03:20:00Z] [error] Container Startup Failure - Permission Denied
- Error: `java.io.FileNotFoundException: /opt/jboss/wildfly/standalone/log/server.log (Permission denied)`
- Root cause: CLI script ran as root, modified standalone directory ownership
- Resolution: Added `chown -R jboss:jboss /opt/jboss/wildfly/standalone` after CLI execution

## [2026-03-17T03:21:00Z] [info] Build Success
- Docker image built successfully
- Maven build: BUILD SUCCESS
- WildFly datasource configured via CLI
- WAR deployed to WildFly deployments directory

## [2026-03-17T03:22:00Z] [info] Container Startup Success
- WildFly 39.0.1.Final started in ~6 seconds
- Application deployed at context path `/address-book`
- JSF (Mojarra 4.0.13) initialized
- H2 database table `Contact` created automatically
- All 466 services started successfully

## [2026-03-17T03:23:00Z] [info] Smoke Test Results - Host
- All 5 tests passed from host machine
- Index page: 200 OK, contains "Welcome to the Address Book"
- Contact list page: 200 OK
- Create contact page: 200 OK, contains form fields
- CSS resource: 200 OK via Jakarta Faces resource handler
- Form validation: All expected fields present

## [2026-03-17T03:24:00Z] [info] Smoke Test Results - Container
- All 5 tests passed from inside the Docker container
- Confirms application fully functional in containerized environment

## [2026-03-17T03:25:00Z] [info] Migration Complete
- Migration from Quarkus to Jakarta EE (WildFly) completed successfully
- All smoke tests pass (5/5)
- Application builds, deploys, and runs correctly
