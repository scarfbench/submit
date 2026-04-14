# CHANGELOG - Quarkus to Spring Boot Migration

## Migration Summary

Migrated PetClinic application from **Quarkus 3.30.5** to **Spring Boot 3.2.5**.

### Build System (pom.xml)

- Replaced Quarkus BOM and plugins with Spring Boot parent `3.2.5` and `spring-boot-maven-plugin`
- Replaced `quarkus-rest`, `quarkus-qute`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-h2`, `quarkus-jdbc-postgresql` with:
  - `spring-boot-starter-web`
  - `spring-boot-starter-thymeleaf`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-validation`
  - `h2` (runtime)
  - `postgresql` (runtime)
  - `jackson-databind`
  - `webjars-locator-core`, `bootstrap` (5.1.3), `font-awesome` (4.7.0)
- Removed all Quarkus Maven plugins and profiles

### Package Structure

- Changed base package from `org.quarkus.samples.petclinic` to `org.springframework.samples.petclinic`
- Maintained sub-package organization: `model`, `owner`, `visit`, `vet`, `system`

### Application Entry Point

- Removed `JaxRsApplication.java` (JAX-RS `@ApplicationPath` with `@Blocking`)
- Created `PetClinicApplication.java` with `@SpringBootApplication` and `main()` method

### Entity Layer

- **Person (base entity)**: Removed `PanacheEntity` extension; added `@MappedSuperclass`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`. Converted public fields to private with getters/setters. Removed `@FormParam` annotations.
- **Owner**: Removed Panache static query methods (`findByLastName`, `attach`). Converted to standard JPA entity with `@OneToMany(fetch = FetchType.EAGER)` for pets. Added `@JsonManagedReference`.
- **Pet**: Converted from Panache to standard JPA. Added `@JsonBackReference` on owner. Visits stored as `@Transient` Set loaded on-demand.
- **PetType**: Converted from Panache to standard JPA. Removed inner `Formatter` class (`@TemplateExtension`).
- **Visit**: Converted from Panache to standard JPA entity.
- **Vet**: Converted from Panache to standard JPA. Added `getNrOfSpecialties()` helper.
- **Specialty**: Converted from Panache to standard JPA.
- **Vets**: Kept as DTO wrapper with `@XmlRootElement`.

### Repository Layer (NEW)

Created Spring Data JPA repositories replacing Panache static methods:
- `OwnerRepository extends JpaRepository<Owner, Long>` with `findByLastName(String)`
- `PetRepository extends JpaRepository<Pet, Long>`
- `PetTypeRepository extends JpaRepository<PetType, Long>` with `findByName(String)`
- `VisitRepository extends JpaRepository<Visit, Long>` with `findByPetId(Long)`
- `VetRepository extends JpaRepository<Vet, Long>`

### Controller Layer

Converted JAX-RS resources to Spring MVC controllers:

- **OwnersResource** -> **OwnerController** (`@Controller`):
  - `@Path`/`@GET`/`@POST` -> `@GetMapping`/`@PostMapping`
  - `@BeanParam OwnerForm` -> `@Valid Owner` with `BindingResult`
  - `@FormParam` -> `@RequestParam`
  - `TemplateInstance` return -> `String` (view name) with `Model`
  - Added `/owners/api/list` JSON endpoint with `@ResponseBody`
- **PetResource** -> **PetController**: Similar conversion; form binding via `@RequestParam`
- **VisitResource** -> **VisitController**: Similar conversion
- **VetResource** -> **VetController**: `/vets.html` for HTML, `/vets` for JSON (`@ResponseBody`)
- **WelcomeResource** -> **WelcomeController**: Returns "welcome" view
- **CrashResource** -> **CrashController**: Throws RuntimeException at `/oups`
- **ErrorExceptionMapper** -> **ErrorController** (`@ControllerAdvice` with `@ExceptionHandler`)

### Removed Classes

- `JaxRsApplication.java` - JAX-RS application class
- `Temporals.java` - Qute `@TemplateExtension` for date formatting
- `OwnerForm.java` - `@BeanParam` form DTO (replaced by direct entity binding)
- `VisitComparator.java` - Replaced by inline sorting
- `SpecialityComparator.java` - Replaced by inline sorting
- `LocalDateConverter.java` - JAX-RS `ParamConverter` (Spring handles dates natively)
- `LocalDateParamConverterProvider.java` - JAX-RS converter provider
- `Templates.java` - Qute `@CheckedTemplate` class
- `TemplatesLocale.java` - Locale-aware template wrapper
- `LocaleVariantCreator.java` - Locale variant handling
- `AppMessages.java` - Qute `@MessageBundle` i18n interface
- `SpanishAppMessages.java` - Spanish `@Localized` translations
- `PetType.Formatter` - Qute template extension inner class

### Template Migration

Converted all 10 Qute templates to Thymeleaf:

- `base.html` -> `fragments/layout.html` (Thymeleaf fragments: `head`, `navbar`, `scripts`)
- `welcome.html` - `{#include base}` -> `th:replace="~{fragments/layout :: ...}"`
- `error.html` - Same fragment pattern
- `vetList.html` - `{#for}` -> `th:each`, `{msg:xxx}` -> hardcoded English
- `findOwners.html` - Form converted with Thymeleaf action
- `ownersList.html` - Table iteration with `th:each`
- `ownerDetails.html` - Owner detail display with Thymeleaf expressions
- `createOrUpdateOwnerForm.html` - Form fields with `th:value`, `th:field`
- `createOrUpdatePetForm.html` - Pet form with type select dropdown
- `createOrUpdateVisitForm.html` - Visit form with pet details

Key template syntax changes:
- `{#include base}...{/include}` -> `th:replace="~{fragments/layout :: head}"` etc.
- `{#for item in list}` -> `th:each="item : ${list}"`
- `{item.property}` -> `th:text="${item.property}"`
- `{#if condition}` -> `th:if="${condition}"`
- `{msg:key}` -> Hardcoded English strings (i18n simplified)
- `{#form}` -> Standard HTML `<form>` with `th:action`

### Configuration

- Converted `application.properties` from Quarkus profile format (`%dev.`, `%test.`, `%prod.`) to Spring Boot format
- H2 in-memory database for development
- `spring.jpa.hibernate.ddl-auto=create-drop` with `spring.sql.init.mode=always` for data seeding
- `spring.jpa.defer-datasource-initialization=true` to load `data.sql` after schema creation

### Static Resources

- Moved from `src/main/resources/META-INF/resources/` to `src/main/resources/static/`
- Compiled SCSS files (`petclinic.scss`, `header.scss`, `typography.scss`, `responsive.scss`) into single `petclinic.css`
- Removed SCSS source files (Spring Boot does not have built-in SCSS compilation)
- Kept all images and font files

### Data

- Renamed `import.sql` to `data.sql` (Spring Boot convention)
- Added explicit column names to all INSERT statements to avoid column ordering issues with JPA-generated schemas

### Dockerfile

- Updated build command from Quarkus to `mvn clean package -DskipTests`
- Updated CMD to `java -jar target/spring-petclinic-1.0.0-SNAPSHOT.jar`

### Smoke Tests

Generated `smoke.py` with 27 tests covering:
- Welcome page (3 tests): HTTP 200, content, navigation
- Veterinarians (4 tests): HTML page, JSON API, specialty data
- Find Owners (4 tests): Form, list all, search by name, single result redirect
- Owner Details (2 tests): Page load, info display
- New Owner (3 tests): Form, fields, creation
- Edit Owner (2 tests): Form load, pre-populated data
- Pets (3 tests): New pet form, type select, edit pet form
- Visits (2 tests): Form, fields
- Owner API (1 test): JSON list endpoint
- Error page (1 test): `/oups` error handling

### Test Results

All 27 smoke tests pass successfully.
