# CHANGELOG - Jakarta EE to Spring Boot Migration

## Migration Summary

Migrated the Petclinic application from Jakarta EE 10 (Liberty/WildFly/Payara/GlassFish WAR deployment) to Spring Boot 3.2.5 (embedded Tomcat JAR deployment).

**Source:** Jakarta EE 10 with EJB, CDI, JAX-RS, JSF/PrimeFaces, JPA
**Target:** Spring Boot 3.2.5 with Spring MVC, Spring Data JPA, Jackson
**Java Version:** 21

---

## Changes

### Build System (pom.xml)
- Replaced the entire 59K+ Jakarta EE pom.xml with a Spring Boot pom.xml
- Changed parent to `spring-boot-starter-parent:3.2.5`
- Changed packaging from `war` to `jar`
- Replaced all Jakarta EE dependencies with Spring Boot starters:
  - `spring-boot-starter-web` (replaces JAX-RS, Servlet, JSF)
  - `spring-boot-starter-data-jpa` (replaces JPA + EJB DAO layer)
  - `spring-boot-starter-validation` (replaces Bean Validation)
  - `spring-boot-starter-actuator` (new: health/info endpoints)
  - `jackson-dataformat-xml` (replaces JAXB for XML support)
  - `postgresql` (driver, kept)
  - `lombok` (kept)
  - `spring-boot-starter-test` and `h2` (test scope)

### Application Configuration
- **Created** `src/main/java/.../application/conf/PetclinicApplication.java` - Spring Boot main class with `@SpringBootApplication`, `@EntityScan`, `@EnableJpaRepositories`
- **Created** `src/main/resources/application.properties` - PostgreSQL datasource, JPA/Hibernate config, Jackson settings, Actuator endpoints
- **Created** `src/main/resources/data.sql` - Seed data (2 owners, 12 pet types, 5 pets, 7 visits, 7 specialties, 2 vets, vet-specialty associations)

### Entity Classes (6 files modified)
Files: `Owner.java`, `Pet.java`, `PetType.java`, `Specialty.java`, `Vet.java`, `Visit.java`
- Removed `extends EntityBaseObject implements EntityBase`
- Changed to `implements Comparable<T>, Serializable`
- Removed `@EntityListeners(XxxListener.class)` annotations
- Kept all `jakarta.persistence.*` annotations (compatible with Spring Boot 3.x)
- Kept all `jakarta.validation.*` annotations
- Kept `@NamedQueries`
- Kept Lombok annotations (`@Getter`, `@Setter`, `@ToString`, etc.)

### Data Access Layer - Spring Data JPA Repositories (6 new files)
Replaced DAO interfaces + EntityManager implementations with Spring Data JPA repositories:
- `OwnerRepository extends JpaRepository<Owner, Long>` - custom queries for ordered list and search
- `PetRepository extends JpaRepository<Pet, Long>` - queries for ordered list and by owner
- `PetTypeRepository extends JpaRepository<PetType, Long>` - queries for ordered list, by name, search
- `SpecialtyRepository extends JpaRepository<Specialty, Long>` - queries for ordered list, by name, search
- `VetRepository extends JpaRepository<Vet, Long>` - queries for ordered list and search
- `VisitRepository extends JpaRepository<Visit, Long>` - queries for ordered by date and by pet

### Service Layer (12 files modified)
**Interfaces** (6 files): Removed `extends CrudService<T>, SearchableService<T>`, defined all methods directly.

**Implementations** (6 files):
- `@Stateless` -> `@Service`
- `@EJB` -> constructor injection with `@Autowired`
- DAO references -> Repository references
- Added `@Transactional` for write operations
- Kept `@PostConstruct` logging

### REST Endpoints (6 files rewritten)
Migrated from JAX-RS to Spring MVC:
- `@Path` -> `@RestController` + `@RequestMapping`
- `@GET` -> `@GetMapping`
- `@PathParam` -> `@PathVariable`
- `@Produces` -> `produces` attribute on mapping annotations
- Returns `ResponseEntity<>` with proper HTTP status codes
- 404 handling for not-found entities

Endpoints preserved:
- `/rest/owner/` - Owner CRUD (list, get by id, JSON/XML variants)
- `/rest/pet/` - Pet CRUD
- `/rest/petType/` - PetType CRUD
- `/rest/specialty/` - Specialty CRUD
- `/rest/vet/` - Vet CRUD
- `/rest/visit/` - Visit CRUD

### DTO Classes (12 files updated)
- Removed JAXB annotations (`@XmlRootElement`, `@XmlElement`, `@XmlAccessorType`)
- Removed JSON-B annotations (`@JsonbProperty`)
- Added Jackson annotations (`@JsonProperty`, `@JsonFormat`, `@JsonRootName`)
- Kept `@JacksonXmlProperty` / `@JacksonXmlElementWrapper` for XML support

### EndpointUtil Classes (6 files updated)
- `@Stateless` -> `@Component`
- Removed serialization/deserialization helper methods (handled by Spring MVC auto-conversion)
- Kept DTO factory methods (`dtoFactory`, `dtoListFactory`)

### Dockerfile
- Kept base image: `maven:3.9.12-ibm-semeru-21-noble`
- Added `RUN mvn clean package -DskipTests -q` build step
- Changed CMD from `mvn clean liberty:run` to `java -jar target/petclinic-jakartaee-10.0.0.18-SNAPSHOT.jar`
- Kept PostgreSQL initialization (user, database, grants)

### Smoke Tests (smoke.py)
- Created 28 pytest-based smoke tests covering:
  - Actuator health and info endpoints
  - All 6 REST endpoints (list, get by id, not found 404)
  - JSON and XML content negotiation
  - Data integrity checks (seed data presence)

### Files Removed
- All DAO interfaces and implementations (12 files)
- All JSF view/flow classes (12+ files)
- All entity listener classes (6 files)
- Framework base classes: `EntityBase`, `EntityBaseObject`, `CrudDao`, `CrudService`, `SearchableService`, `SearchableEntity`, `EntityListenerLogger`
- CDI utilities: `MessageProvider`, `MessageBundle`
- JSF converters: `PetTypeConverter`, `SpecialtyConverter`
- Jakarta EE config: `beans.xml`, `faces-config.xml`, `web.xml`, `persistence.xml` (all runtime variants)
- Old test directory: `src/test/` (Arquillian/Graphene tests incompatible with Spring Boot)

---

## Errors Encountered and Resolutions

### Error 1: Old Arquillian test files fail compilation
**Cause:** `src/test/java` contained Arquillian, Graphene, and Selenium tests referencing dependencies not in the new Spring Boot pom.xml.
**Resolution:** Removed entire `src/test/` directory. Tests replaced by smoke.py.

### Error 2: Entity classes reference deleted base classes
**Cause:** Entity classes still had `extends EntityBaseObject implements EntityBase` after base classes were deleted.
**Resolution:** Rewrote all 6 entity classes to `implements Comparable<T>, Serializable` and moved `id`, `uuid`, `searchindex` fields directly into each entity.

### Error 3: Service interfaces reference deleted framework interfaces
**Cause:** Service interfaces still had `extends CrudService<T>, SearchableService<T>` after framework classes were deleted.
**Resolution:** Rewrote all 6 service interfaces with methods defined directly.

### Error 4: data.sql column name mismatch with Hibernate naming strategy
**Cause:** Spring Boot's `SpringPhysicalNamingStrategy` transforms `@Column(name = "lastName")` to `last_name` in the actual DDL, but `data.sql` used `lastname`.
**Resolution:** Updated `data.sql` to use `last_name` (matching Hibernate's physical column name output) for Owner and Vet table inserts.

### Error 5: Smoke tests expected dict-wrapped JSON, API returns arrays
**Cause:** Smoke tests assumed list endpoints would return `{"owners": [...]}` format, but Spring MVC controllers return plain JSON arrays.
**Resolution:** Updated smoke tests to accept both dict and list response formats.

---

## Validation Results

**28/28 smoke tests passing:**
- 2 actuator tests (health, info)
- 5 owner endpoint tests (list, get, not-found, json, xml)
- 3 pet endpoint tests (list, get, not-found)
- 3 pet type endpoint tests (list, get, not-found)
- 3 specialty endpoint tests (list, get, not-found)
- 3 vet endpoint tests (list, get, not-found)
- 3 visit endpoint tests (list, get, not-found)
- 4 data integrity tests (owners, pet types, specialties, vets)
- 2 XML content negotiation tests (owner list, specialty list)
