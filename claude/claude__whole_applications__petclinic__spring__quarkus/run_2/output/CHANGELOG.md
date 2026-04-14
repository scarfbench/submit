# CHANGELOG - Spring Boot to Quarkus Migration

## Migration Summary

Migrated the Spring PetClinic application from Spring Boot 3.3.0 to Quarkus 3.17.8.
All 13 smoke tests pass. Application starts in ~3.2 seconds on JVM.

---

## Actions Performed

### 1. Codebase Analysis
- Analyzed Spring Boot 3.3.0 application structure: Spring MVC controllers, Thymeleaf templates, Spring Data JPA repositories, H2 in-memory database
- Identified 5 controllers (Owner, Pet, Visit, Vet, Welcome/Crash), 3 Spring Data JPA repositories, 8 JPA entities, Thymeleaf templates with layout fragment system
- Identified Spring-specific features requiring replacement: `@Controller`/`@GetMapping`/`@PostMapping`, `@ModelAttribute`, `@InitBinder`, `WebDataBinder`, `BindingResult`, `RedirectAttributes`, `Page<T>`/`Pageable`, Thymeleaf templates, Spring Data JPA interfaces

### 2. Smoke Tests Created
- Created `smoke.py` with 13 test cases covering: home page, find owners, owners list, owner details, vets HTML, vets JSON API, create owner, search by last name, error page, static CSS, new pet form, new visit form, edit owner form
- Tests use `requests` library with configurable `BASE_URL` environment variable

### 3. Build System (pom.xml)
- Removed Spring Boot parent POM (`spring-boot-starter-parent`)
- Added Quarkus BOM 3.17.8 via `<dependencyManagement>`
- Dependencies added: `quarkus-arc`, `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-rest-qute`, `quarkus-hibernate-orm`, `quarkus-jdbc-h2`, `quarkus-hibernate-validator`, `quarkus-smallrye-health`
- Dependencies removed: all `spring-boot-starter-*`, `caffeine`, `webjars`, `testcontainers`, `checkstyle`
- Added `quarkus-maven-plugin` and configured uber-jar packaging
- Test dependencies: `quarkus-junit5`, `rest-assured`

### 4. Application Configuration
- Created `application.properties` in Quarkus format
- H2 in-memory database: `jdbc:h2:mem:petclinic`
- Hibernate ORM: `drop-and-create` schema generation with `import.sql` seed data
- HTTP port: 8080

### 5. Database Initialization (import.sql)
- Replaced Spring's `schema.sql`/`data.sql` with Quarkus-compatible `import.sql`
- Seed data: 6 vets, 3 specialties, 5 vet-specialty associations, 6 pet types, 10 owners, 13 pets, 4 visits
- Used auto-generated IDs (no explicit ID columns) to work with `@GeneratedValue(strategy = GenerationType.IDENTITY)`

### 6. Java Source Refactoring

#### Entities (Minimal Changes)
- Kept JPA annotations unchanged (`@Entity`, `@Table`, `@ManyToOne`, `@OneToMany`, `@ManyToMany`, etc.)
- Removed Spring-specific imports: `@DateTimeFormat`, `ToStringCreator`, `Assert`
- Removed JAXB annotations (`@XmlRootElement`, `@XmlElement`) from `Vets.java`, replaced with Jackson `@JsonProperty`
- `BaseEntity`: Retained `@GeneratedValue(strategy = GenerationType.IDENTITY)`

#### Repositories (Rewritten)
- Replaced Spring Data JPA interfaces with CDI beans (`@ApplicationScoped`)
- `OwnerRepository`: Uses `@Inject EntityManager` with JPQL queries, manual pagination (`setFirstResult`/`setMaxResults`)
- `PetTypeRepository`: EntityManager-based `findPetTypes()` and `findById()`
- `VetRepository`: EntityManager-based with pagination support

#### Controllers -> JAX-RS Resources (Rewritten)
- `OwnerResource` (`@Path("/owners")`): Find, list, create, show, edit owners with `@FormParam` processing
- `PetResource` (`@Path("/owners/{ownerId}/pets")`): New/edit pet forms with manual validation
- `VisitResource` (`@Path("/owners/{ownerId}/pets/{petId}/visits")`): New visit form
- `VetResource` (`@Path("/")`): `/vets.html` (HTML) and `/vets` (JSON) endpoints
- `WelcomeResource` (`@Path("/")`): Home page
- `CrashResource` (`@Path("/oups")`): Error demonstration page

#### Deleted Files
- `PetClinicApplication.java` (Spring Boot main class)
- `PetClinicRuntimeHints.java` (Spring native hints)
- `CacheConfiguration.java` (Spring cache config)
- `WebConfiguration.java` (Spring web config)
- All Spring MVC controllers
- `PetTypeFormatter.java`, `PetValidator.java`
- All Spring Data repository interfaces
- All Thymeleaf templates
- `application-postgres.properties`, `application-mysql.properties`
- `banner.txt`
- `src/test/` directory
- `src/checkstyle/` directory

### 7. Templates (Thymeleaf -> Qute)
- Replaced all Thymeleaf templates with Qute templates
- Base layout: `layout.html` using `{#include}`/`{#insert}` pattern
- Used Qute operators: `{#for}`, `{#if}`, `{??}` (optional check), `{?:}` (elvis), `{.orEmpty}`
- Templates: `welcome.html`, `error.html`, `findOwners.html`, `ownersList.html`, `ownerDetails.html`, `createOrUpdateOwnerForm.html`, `createOrUpdatePetForm.html`, `createOrUpdateVisitForm.html`, `vetList.html`

### 8. Static Resources
- Moved from `src/main/resources/static/resources/` to `src/main/resources/META-INF/resources/resources/` (Quarkus convention)

### 9. Dockerfile
- Updated build command: `mvn clean package -DskipTests -Dquarkus.package.jar.type=uber-jar`
- Updated run command: `java -jar target/spring-petclinic-3.5.0-SNAPSHOT-runner.jar`

---

## Errors Encountered and Resolutions

### Error 1: Maven Dependency Resolution Failure
- **Symptom**: `quarkus-resteasy-reactive`, `quarkus-resteasy-reactive-jackson`, `quarkus-resteasy-reactive-qute` not found in Quarkus BOM 3.17.8
- **Cause**: Artifacts were renamed in Quarkus 3.9+ from `quarkus-resteasy-reactive-*` to `quarkus-rest-*`
- **Resolution**: Changed dependency artifact IDs to `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-rest-qute`

### Error 2: Qute Template Variable Not Found
- **Symptom**: 500 error - "Key 'error' not found in the template data map" in `findOwners.html`
- **Cause**: Qute strict mode requires all referenced variables to exist in the data map. `{#if error}` fails when `error` key is not present
- **Resolution**: Changed `{#if error}` to `{#if error??}` (null-safe check) and `{lastName ?: ''}` to `{lastName.orEmpty}`

### Error 3: Qute Template Variable Not Found (message)
- **Symptom**: 500 error - "Key 'message' not found in the template data map" in `ownerDetails.html`
- **Cause**: `message` variable only present after redirect from create/update operations, not on direct GET
- **Resolution**: Changed `{#if message}` to `{#if message??}`

### Error 4: Primary Key Violation in import.sql
- **Symptom**: SQL error during startup - primary key constraint violation when inserting seed data
- **Cause**: `@GeneratedValue(strategy = GenerationType.IDENTITY)` auto-generates IDs via H2 identity columns, conflicting with explicit ID values in INSERT statements
- **Resolution**: Removed explicit `id` columns from all INSERT statements, relying on auto-generated sequential IDs. Insertion order ensures foreign key references remain valid

### Error 5: Qute Template Variable Not Found (errors)
- **Symptom**: 500 error on owner/pet/visit form pages when `errors` list not passed to template
- **Cause**: `errors` only added to template data map on validation failure, but Qute requires all variables referenced in template to exist
- **Resolution**: Changed `{#if errors}` to `{#if errors??}` in all form templates (createOrUpdateOwnerForm, createOrUpdatePetForm, createOrUpdateVisitForm)

---

## Validation Results

### Smoke Test Results: 13/13 PASSED

| # | Test | Status |
|---|------|--------|
| 1 | Home page loads | PASS |
| 2 | Find owners page loads | PASS |
| 3 | Owners list with data | PASS |
| 4 | Owner details (ID=1) | PASS |
| 5 | Vets HTML page | PASS |
| 6 | Vets JSON API | PASS |
| 7 | Create new owner | PASS |
| 8 | Search owners by last name | PASS |
| 9 | Error page (oups) | PASS |
| 10 | Static CSS resource | PASS |
| 11 | New pet form | PASS |
| 12 | New visit form | PASS |
| 13 | Edit owner form | PASS |

### Application Startup
- Quarkus 3.17.8 on JVM started in ~3.2 seconds
- Installed features: agroal, cdi, hibernate-orm, hibernate-validator, jdbc-h2, narayana-jta, qute, rest, rest-jackson, rest-qute, smallrye-context-propagation, smallrye-health, vertx
