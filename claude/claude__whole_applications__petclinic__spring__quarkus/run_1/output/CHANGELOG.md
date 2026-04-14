# CHANGELOG: Spring Boot to Quarkus Migration

## Migration Summary

Migrated the Spring PetClinic application from Spring Boot 3.x to Quarkus 3.17.8 while preserving all existing functionality including owner/pet/visit management and veterinarian listing.

## Actions Taken

### 1. Build Configuration (pom.xml)
- Removed Spring Boot parent POM and all Spring Boot dependencies
- Added Quarkus BOM 3.17.8 as dependency management
- Added Quarkus extensions: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-rest-qute`, `quarkus-hibernate-orm`, `quarkus-jdbc-h2`, `quarkus-hibernate-validator`, `quarkus-arc`, `quarkus-smallrye-health`, `quarkus-cache`, `quarkus-webjars-locator`
- Added webjars for Bootstrap 5.3.6 and Font Awesome 4.7.0
- Configured `quarkus-maven-plugin` for build, and `maven-compiler-plugin` with `-parameters` flag

### 2. Application Configuration
- Replaced `application.properties` with Quarkus-compatible configuration
- Configured H2 in-memory datasource, Hibernate ORM (drop-and-create), HTTP port 8080
- Created `import.sql` with all seed data (vets, specialties, pet types, owners, pets, visits)

### 3. Model Entities
- Removed Spring-specific imports (`ToStringCreator`, `Assert`, `DateTimeFormat`)
- Replaced `ToStringCreator` usage in `Owner.java` with manual `toString()`
- Replaced `Assert.notNull()` with `Objects.requireNonNull()`
- Removed `@DateTimeFormat` annotations from `Pet.java` and `Visit.java`
- Kept all `jakarta.persistence` annotations unchanged

### 4. Repository Layer
- Replaced Spring Data JPA interfaces with CDI `@ApplicationScoped` beans using `EntityManager`
- `OwnerRepository`: findById, findByLastNameStartingWith (manual pagination), countByLastNameStartingWith, save
- `PetTypeRepository`: findPetTypes, findById
- `VetRepository`: findAll, findPaginated, count (with `@CacheResult` for caching)

### 5. Controllers to JAX-RS Resources
- Replaced Spring MVC `@Controller`/`@GetMapping`/`@PostMapping` with JAX-RS `@Path`/`@GET`/`@POST`
- `WelcomeResource` (@Path("/")): Home page
- `CrashResource` (@Path("/oups")): Error demonstration endpoint
- `VetResource` (@Path("/")): /vets.html (HTML) and /vets (JSON)
- `OwnerResource` (@Path("/owners")): All owner, pet, and visit endpoints consolidated into single resource class to avoid JAX-RS path conflicts

### 6. Template Engine Migration
- Converted all Thymeleaf templates to Qute templates
- Replaced `th:*` attributes with Qute `{expression}` syntax
- Replaced Thymeleaf layout dialect with Qute `{#include}`/`{#insert}` pattern
- Replaced Thymeleaf iteration (`th:each`) with Qute `{#for}`
- Used Qute iteration metadata (`pet_hasNext`, `spec_hasNext`) instead of Thymeleaf's `isLast`
- Used Qute null-safe operators (`??` and `?:`) for optional template data keys
- Used Qute integer range iteration for pagination (1-based indexing)

### 7. Static Resources
- Moved static resources from `src/main/resources/static/` to `src/main/resources/META-INF/resources/` (Quarkus convention)

### 8. Dockerfile
- Updated build command to `mvn clean package -DskipTests`
- Updated run command to `java -jar target/quarkus-app/quarkus-run.jar`

### 9. Deleted Spring-Specific Files
- `PetClinicApplication.java` (Spring Boot main class)
- `PetClinicRuntimeHints.java` (Spring AOT hints)
- `CacheConfiguration.java` (Spring cache config)
- `WebConfiguration.java` (Spring web config)
- `PetValidator.java` (Spring Validator)
- `PetTypeFormatter.java` (Spring Formatter)
- All Spring MVC controllers (replaced by JAX-RS resources)
- All test files (Quarkus test framework differs)
- `docker-compose.yml`, `k8s/` directory, checkstyle configs

## Errors Encountered and Resolutions

### Error 1: Qute Template `isLast` Not Found
- **Symptom**: 500 errors on pages with comma-separated lists (owners list, vet specialties)
- **Cause**: Qute uses `{iterVar_hasNext}` for iteration metadata, not standalone `isLast`
- **Fix**: Changed `{#if !isLast}` to `{#if pet_hasNext}` and `{#if spec_hasNext}` in templates

### Error 2: JAX-RS Path Conflicts Causing 404 Errors
- **Symptom**: 404 errors on /owners/find, /owners/new, /owners/{id}, /owners/{id}/edit
- **Cause**: Separate `PetResource` with `@Path("/owners/{ownerId}")` captured all `/owners/*` requests before `OwnerResource` could match literal path segments like `/find`, `/new`
- **Fix**: Consolidated all pet and visit endpoints into `OwnerResource.java`, deleted separate `PetResource.java` and `VisitResource.java`

### Error 3: Qute Strict Mode Key Not Found
- **Symptom**: 500 errors on /owners/find, /owners/new, /owners/{id}/edit
- **Cause**: Qute's strict rendering mode throws exceptions when `{#if key}` references a key not present in the template data map (e.g., `noResults`, `errors` only conditionally provided)
- **Fix**: Changed `{#if noResults}` to `{#if noResults??}` and `{#if errors}` to `{#if errors??}` to use Qute's null-safe check operator

### Error 4: Qute Integer Range Iteration (1-based)
- **Symptom**: Pagination page numbers were off by one
- **Cause**: Qute `{#for i in totalPages}` iterates from 1 to N (inclusive), unlike typical 0-based iteration
- **Fix**: Changed `{i + 1}` to `{i}` in pagination links and active page checks

## Smoke Test Results

```
Results: 12 passed, 0 failed out of 12 tests
```

All endpoints verified:
- Welcome page (GET /)
- Find owners form (GET /owners/find)
- Owners list with pagination (GET /owners?lastName=)
- Owner details (GET /owners/1)
- Vets JSON (GET /vets)
- Vets HTML with pagination (GET /vets.html)
- New owner form (GET /owners/new)
- Create owner (POST /owners/new)
- Search owner (GET /owners?lastName=Davis)
- Edit owner form (GET /owners/1/edit)
- New pet form (GET /owners/1/pets/new)
- New visit form (GET /owners/1/pets/1/visits/new)
