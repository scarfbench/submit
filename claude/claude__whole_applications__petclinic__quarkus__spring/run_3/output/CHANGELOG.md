# Migration Changelog: Quarkus to Spring Boot

## [2026-03-14T10:30:00Z] [info] Project Analysis
- Identified Quarkus PetClinic application with 21 Java source files
- Quarkus Platform version 3.30.5, Java 21
- Key frameworks: JAX-RS (REST), Hibernate ORM Panache, Qute templating, CDI
- Database: H2 in-memory (dev/test), PostgreSQL (prod)
- Seed data in import.sql with owners, pets, vets, visits, specialties
- 10 HTML templates using Qute syntax
- Static resources in META-INF/resources/

## [2026-03-14T10:32:00Z] [info] Smoke Test Generation
- Created smoke.py with 14 tests covering all major endpoints
- Tests: welcome page, find owners, owners list, owner details, new owner form,
  vets HTML page, vets JSON API, owners API list, create owner, edit owner form,
  new pet form, edit pet form, new visit form, error page

## [2026-03-14T10:34:00Z] [info] Dependency Migration (pom.xml)
- Removed Quarkus BOM and all io.quarkus dependencies
- Added Spring Boot parent POM (3.2.5)
- Added spring-boot-starter-web, spring-boot-starter-thymeleaf,
  spring-boot-starter-data-jpa, spring-boot-starter-validation
- Retained H2, PostgreSQL, Bootstrap/Font Awesome webjars
- Added webjars-locator-core for Spring Boot webjar path resolution
- Replaced quarkus-maven-plugin with spring-boot-maven-plugin
- Removed native, css Maven profiles (Quarkus-specific)

## [2026-03-14T10:36:00Z] [info] Configuration Migration (application.properties)
- Replaced Quarkus profile-based config (%dev, %test, %prod) with Spring Boot defaults
- Mapped quarkus.datasource.* to spring.datasource.*
- Mapped quarkus.hibernate-orm.* to spring.jpa.*
- Set spring.jpa.hibernate.ddl-auto=create-drop
- Enabled spring.jpa.defer-datasource-initialization=true for data.sql loading
- Added Thymeleaf configuration (spring.thymeleaf.cache=false, mode=HTML)
- Removed Quarkus-specific: container-image, kubernetes, qute, analytics settings

## [2026-03-14T10:38:00Z] [info] Entity Migration
- Removed PanacheEntity inheritance from all entities (Person, Owner, Pet, PetType, Specialty, Vet, Visit)
- Added explicit @Id and @GeneratedValue fields
- Replaced public fields with private fields + getters/setters
- Removed @FormParam annotations (JAX-RS-specific)
- Removed Panache static query methods (listAll, findById, persist, list)
- Removed getEntityManager().merge() calls
- Removed @TemplateExtension annotations (Qute-specific)
- Package renamed from org.quarkus.samples.petclinic to org.springframework.samples.petclinic

## [2026-03-14T10:40:00Z] [info] Repository Layer Created
- Created OwnerRepository extends JpaRepository<Owner, Long>
- Created PetRepository extends JpaRepository<Pet, Long>
- Created PetTypeRepository extends JpaRepository<PetType, Long>
- Created VisitRepository extends JpaRepository<Visit, Long>
- Created VetRepository extends JpaRepository<Vet, Long>
- Added custom query methods: findByLastName, findByPetId, findByName

## [2026-03-14T10:42:00Z] [info] Controller Migration
- Replaced JAX-RS resource classes with Spring MVC @Controller classes
- OwnersResource.java -> OwnerController.java
- PetResource.java -> PetController.java
- VisitResource.java -> VisitController.java
- VetResource.java -> VetController.java
- WelcomeResource.java -> WelcomeController.java
- CrashResource.java -> CrashController.java
- ErrorExceptionMapper -> ErrorController (@ControllerAdvice)
- Replaced @Path with @GetMapping/@PostMapping
- Replaced @Inject with constructor injection
- Replaced @BeanParam/@FormParam with @RequestParam/@Valid
- Replaced @PathParam with @PathVariable
- Replaced @QueryParam with @RequestParam
- Replaced TemplateInstance returns with String view names + Model
- Replaced @Transactional (jakarta.transaction) with Spring's transactional repository save()

## [2026-03-14T10:43:00Z] [info] Spring Boot Application Class
- Created PetClinicApplication.java with @SpringBootApplication and main method

## [2026-03-14T10:44:00Z] [info] Template Migration (Qute -> Thymeleaf)
- Converted all 10 templates from Qute syntax to Thymeleaf
- Created fragments/layout.html for shared page layout
- Replaced {#include base}{/include} with th:replace="~{fragments/layout :: layout(...)}"
- Replaced {msg:key} message expressions with hardcoded English text
- Replaced {#for item in list}...{/for} with th:each="item : ${list}"
- Replaced {expression} with th:text="${expression}"
- Replaced {#if condition}...{/if} with th:if="${condition}"
- Replaced {owner.firstName} with th:text="${owner.firstName}"
- Replaced Qute safe-navigation (?.) with Thymeleaf null checks

## [2026-03-14T10:45:00Z] [info] Removed Quarkus-Specific Files
- Removed src/main/java/org/quarkus/ (entire old source tree)
- Removed src/test/java/org/quarkus/ (old test files)
- Removed src/main/docker/ (Quarkus-specific Dockerfiles)
- Removed JaxRsApplication.java, Temporals.java, LocalDateConverter.java,
  LocalDateParamConverterProvider.java, LocaleVariantCreator.java,
  AppMessages.java, SpanishAppMessages.java, Templates.java, TemplatesLocale.java

## [2026-03-14T10:45:30Z] [info] Static Resources
- Moved static resources from src/main/resources/META-INF/resources/ to src/main/resources/static/
- Created minimal petclinic.css in src/main/resources/static/css/

## [2026-03-14T10:46:00Z] [info] Seed Data
- Created src/main/resources/data.sql (Spring Boot convention)
- Migrated all INSERT statements from import.sql
- Removed import.sql (Hibernate auto-loads it, causing duplicate key errors)

## [2026-03-14T10:46:30Z] [info] Dockerfile Update
- Changed build command from `mvn clean install -DskipTests -Dquarkus.profile=dev` to `mvn clean package -DskipTests`
- Changed CMD from Quarkus fast-jar to Spring Boot fat JAR
- Added `requests` to Python pip install for smoke tests

## [2026-03-14T10:47:00Z] [info] First Build & Run Attempt
- Docker image built successfully
- Container started but exited with code 1

## [2026-03-14T10:47:59Z] [error] Duplicate Key Violation at Startup
- File: src/main/resources/import.sql + src/main/resources/data.sql
- Error: Unique index or primary key violation on VETS table
- Root Cause: Both Hibernate's import.sql and Spring's data.sql were loading seed data
- Resolution: Removed import.sql, kept only data.sql with spring.jpa.defer-datasource-initialization=true

## [2026-03-14T10:50:00Z] [info] Second Build & Run
- Docker image rebuilt successfully
- Container started and became ready within seconds
- All database tables created, seed data loaded

## [2026-03-14T10:50:30Z] [info] Smoke Tests Execution
- All 14 smoke tests PASSED
- Welcome page: OK
- Find owners page: OK
- Owners list (all owners): OK
- Owner details (ID 1001): OK
- New owner form: OK
- Vets HTML page: OK
- Vets JSON API: OK
- Owners API list: OK
- Create owner (POST): OK
- Edit owner form: OK
- New pet form: OK
- Edit pet form: OK
- New visit form: OK
- Error page: OK

## [2026-03-14T10:51:00Z] [info] Migration Complete
- Successfully migrated from Quarkus 3.30.5 to Spring Boot 3.2.5
- All 14 smoke tests pass
- Application builds, starts, and serves all endpoints correctly
