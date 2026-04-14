# CHANGELOG - Spring Boot to Quarkus Migration

## Migration Summary

Complete migration of the Spring PetClinic application from Spring Boot 3.3.0 to Quarkus 3.8.4. All original functionality is preserved, including owner CRUD, pet management, visit management, veterinarian listing (HTML and JSON), and static resource serving.

## Changes by Category

### Build System (pom.xml)

- Removed Spring Boot parent POM (`spring-boot-starter-parent:3.3.0`)
- Removed all `spring-boot-starter-*` dependencies (web, thymeleaf, data-jpa, validation, test, cache, etc.)
- Added Quarkus BOM `3.8.4` as dependency management
- Added Quarkus extensions:
  - `quarkus-resteasy` (JAX-RS REST endpoints)
  - `quarkus-resteasy-jackson` (JSON serialization)
  - `quarkus-resteasy-qute` (Qute template engine integration)
  - `quarkus-hibernate-orm` (JPA/Hibernate ORM)
  - `quarkus-jdbc-h2` (H2 database driver)
  - `quarkus-hibernate-validator` (Bean Validation)
  - `quarkus-arc` (CDI dependency injection)
  - `quarkus-smallrye-health` (health endpoint)
  - `jakarta.xml.bind-api` and `jaxb-runtime` (XML binding)
- Replaced `spring-boot-maven-plugin` with `quarkus-maven-plugin`
- Configured uber-jar packaging

### Configuration (application.properties)

- Replaced Spring Boot configuration with Quarkus-style properties
- Configured H2 in-memory database via `quarkus.datasource.*`
- Configured Hibernate ORM via `quarkus.hibernate-orm.*`
- Set `quarkus.hibernate-orm.database.generation=drop-and-create`
- Set `quarkus.hibernate-orm.sql-load-script=import.sql`
- Mapped health endpoint to `/actuator/health` via SmallRye Health

### Entity Classes

- **BaseEntity**: No changes (standard JPA annotations)
- **NamedEntity**: No changes
- **Person**: No changes
- **Owner**: Removed Spring `Assert` and `ToStringCreator` imports, replaced with plain Java equivalents
- **Pet**: Removed `@DateTimeFormat` Spring annotation
- **PetType**: No changes
- **Visit**: Removed `@DateTimeFormat` Spring annotation
- **Vet**: Removed `@XmlElement` annotation
- **Specialty**: No changes
- **Vets**: Removed JAXB annotations, added setter for Jackson compatibility

### Repository Layer

- **OwnerRepository**: Replaced Spring Data JPA `JpaRepository` interface with CDI `@ApplicationScoped` bean using `EntityManager` directly. Methods: `findById`, `findByLastNameStartingWith` (with pagination), `countByLastNameStartingWith`, `save`
- **PetTypeRepository**: New CDI bean with `EntityManager`. Methods: `findPetTypes`, `findByName`
- **VetRepository**: New CDI bean with `EntityManager`. Methods: `findAll` (with and without pagination), `count`

### Controller -> Resource Layer

All Spring MVC `@Controller` classes were replaced with JAX-RS `@Path` resources:

- **OwnerController -> OwnerResource** (`@Path("/owners")`):
  - `@GetMapping` -> `@GET`, `@PostMapping` -> `@POST`
  - `@ModelAttribute` + `BindingResult` -> `@FormParam` with manual validation
  - `ModelAndView`/`String` returns -> `TemplateInstance` (Qute) or `Response.seeOther()` for redirects
  - Methods: `initFindForm`, `processFindForm` (with pagination), `initCreationForm`, `processCreationForm`, `showOwner`, `initUpdateOwnerForm`, `processUpdateOwnerForm`

- **PetController -> PetResource** (`@Path("/owners")`):
  - Method paths include `/{ownerId: [0-9]+}/pets/...` pattern
  - Methods: `initCreationForm`, `processCreationForm`, `initUpdateForm`, `processUpdateForm`

- **VisitController -> VisitResource** (`@Path("/owners")`):
  - Method paths include `/{ownerId: [0-9]+}/pets/{petId: [0-9]+}/visits/...` pattern
  - Methods: `initNewVisitForm`, `processNewVisitForm`

- **VetController -> VetResource** (`@Path("/")`):
  - `/vets.html` returns HTML via Qute template
  - `/vets` returns JSON via Jackson serialization

- **WelcomeController -> WelcomeResource** (`@Path("/")`):
  - Returns Qute welcome template

- **CrashController -> CrashResource** (`@Path("/oups")`):
  - Throws RuntimeException for error page testing

### Template Engine (Thymeleaf -> Qute)

All Thymeleaf templates were rewritten as Qute templates:

- **Layout**: `tags/layout.html` using Qute tag include mechanism with `{#insert /}` for content injection
- **welcome.html**: Uses `{#include tags/layout}...{/include}` pattern
- **findOwners.html**: Qute form with `{#if}` conditionals
- **ownersList.html**: Qute iteration with `{#for}`, pagination logic
- **ownerDetails.html**: Owner information display with pet/visit listings
- **createOrUpdateOwnerForm.html**: Form with error display
- **createOrUpdatePetForm.html**: Pet form with type selection
- **createOrUpdateVisitForm.html**: Visit form with pet information
- **vetList.html**: Vet listing with specialty display and pagination

Key Qute syntax changes from Thymeleaf:
- `th:each="item : ${list}"` -> `{#for item in list}`
- `th:text="${var}"` -> `{var}`
- `th:if="${condition}"` -> `{#if condition}`
- `th:href="@{/path}"` -> `href="/path"`
- Layout: `th:replace="~{fragments/layout :: layout(~{::body})}"` -> `{#include tags/layout}...{/include}`

### Static Resources

- Moved from `src/main/resources/static/` to `src/main/resources/META-INF/resources/` (Quarkus convention)
- CSS, images, fonts all preserved at original URL paths

### Database Initialization

- Created `src/main/resources/import.sql` with all seed data (matching original Spring PetClinic data)
- Includes INSERT statements for: vets (6), specialties (3), vet_specialties (5), types (6), owners (10), pets (13), visits (4)
- Added `ALTER TABLE ... ALTER COLUMN id RESTART WITH 100` statements to prevent identity counter conflicts when creating new records via the application

### Dockerfile

- Base image: `maven:3.9.12-ibm-semeru-21-noble` (retained)
- Build command: `mvn clean package -DskipTests -Dquarkus.package.type=uber-jar`
- Run command: `java -jar target/spring-petclinic-3.5.0-SNAPSHOT-runner.jar`

### Deleted Files

- `PetClinicApplication.java` (Spring Boot main class - Quarkus has its own bootstrap)
- `PetClinicRuntimeHints.java` (Spring Native hints - not needed)
- `CacheConfiguration.java` (Spring Cache config - not ported)
- `WebConfiguration.java` (Spring MVC config - not needed)
- `PetTypeFormatter.java` (Spring Formatter - replaced by direct string handling)
- `PetValidator.java` (Spring Validator - replaced by inline validation)
- `package-info.java` files
- All Spring test files (`PetClinicIntegrationTests.java`, etc.)
- `application-mysql.properties`, `application-postgres.properties` (DB-specific profiles)
- `banner.txt` (Spring Boot banner)
- `db/` directory (Spring schema/data SQL files)
- `messages/` directory (Spring message bundles)
- `src/checkstyle/` directory

### Smoke Tests

- Created `smoke.py` with 8 comprehensive tests:
  1. Welcome page (GET /, status 200, content check)
  2. Find owners page (GET /owners/find, status 200)
  3. Owners list (GET /owners?lastName=, status 200)
  4. Owner details (GET /owners/1, status 200, content check for "George"/"Franklin")
  5. Vets HTML page (GET /vets.html, status 200)
  6. Vets JSON API (GET /vets, status 200, JSON structure validation)
  7. Create owner (POST /owners/new, status 200 after redirect, content check)
  8. Static resources (GET /resources/css/petclinic.css, status 200)

## Issues Encountered and Resolved

1. **Qute layout tag rendering**: Initial use of `{nested-content}` in layout tag caused "Key not found" error. Fixed by using `{#insert /}` which is the correct Qute tag include mechanism.

2. **JAX-RS path parameter conflict**: `@Path("/{ownerId}")` matched literal path segments like "find" and "new", causing 404 errors on `/owners/find`. Fixed by adding regex constraints: `@Path("/{ownerId: [0-9]+}")`.

3. **Route conflict between resources**: `PetResource` class-level `@Path("/owners/{ownerId: [0-9]+}")` consumed `/owners/1` requests before `OwnerResource` could handle them. Fixed by moving all resources to `@Path("/owners")` class-level and differentiating at method-level paths.

4. **H2 identity counter conflict**: `import.sql` inserts with explicit IDs did not advance H2's auto-increment counter, causing duplicate key violations on new inserts. Fixed by adding `ALTER TABLE ... ALTER COLUMN id RESTART WITH 100` statements after seed data.

## Test Results

All 8 smoke tests pass:
```
Results: 8 passed, 0 failed out of 8 tests
```
