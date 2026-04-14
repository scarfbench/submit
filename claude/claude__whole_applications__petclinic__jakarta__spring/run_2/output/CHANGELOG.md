# CHANGELOG - Jakarta EE 10 to Spring Boot 3.2.5 Migration

## Migration Summary

Migrated the Petclinic application from Jakarta EE 10 (OpenLiberty) to Spring Boot 3.2.5, preserving all REST API endpoints and seed data.

## Changes

### Dependencies (pom.xml)

- **Replaced** entire Jakarta EE 10 POM (multi-runtime: Liberty, Wildfly, Payara, Glassfish, TomEE) with Spring Boot 3.2.5 parent POM
- **Added** `spring-boot-starter-web` (replaces JAX-RS)
- **Added** `spring-boot-starter-data-jpa` (replaces manual EntityManager DAOs)
- **Added** `spring-boot-starter-validation` (replaces Jakarta Bean Validation standalone)
- **Added** `spring-boot-starter-actuator` (health/info endpoints)
- **Added** `jackson-dataformat-xml` (XML content negotiation, matching original API)
- **Kept** `postgresql` driver (runtime scope)
- **Kept** `lombok` (optional)
- **Added** `spring-boot-starter-test` (test scope)
- **Removed** all Jakarta EE APIs (jakartaee-api, jakartaee-web-api)
- **Removed** JSF/PrimeFaces/OmniFaces dependencies
- **Removed** OpenLiberty Maven plugin and all runtime-specific plugins
- **Removed** EJB, CDI, JAX-RS implementation dependencies
- **Packaging** changed from `war` to `jar`

### Application Bootstrap

- **Created** `PetclinicApplication.java` with `@SpringBootApplication`
- **Created** `application.properties` with PostgreSQL config, context-path `/petclinic`, `ddl-auto=create`, `defer-datasource-initialization=true`
- **Configured** `PhysicalNamingStrategyStandardImpl` to preserve exact column names from original schema (some columns use camelCase like `lastName`, `housenumber`)

### Entity Layer (6 entities preserved)

All entities kept in their original `*.db` sub-packages with `jakarta.persistence.*` and `jakarta.validation.*` annotations (compatible with Spring Boot 3.x):

- **Owner** (`owner` table) - preserved non-standard column names: `lastName`, `housenumber`, `phonenumber`, `zipcode`, `first_name`, `address_info`, `email`
- **Pet** (`owner_pet` table) - FK `owner_pet_pettype_id` to PetType, FK `owner_id` to Owner
- **PetType** (`owner_pet_pettype` table)
- **Specialty** (`specialty` table)
- **Vet** (`vet` table) - column `lastName` (not `last_name`), `@ManyToMany` via `vet_specialties` join table
- **Visit** (`owner_pet_visit` table) - column `visit_date`, FK `owner_pet_id` to Pet

All entities use `@SequenceGenerator` with `allocationSize=1` matching original sequences.

### DAO/Repository Layer

- **Removed** all `*DaoImpl.java` classes (manual EntityManager CRUD)
- **Removed** all `*Dao.java` interfaces
- **Created** Spring Data JPA repositories:
  - `OwnerRepository` - with `@Query` search method and `findAllByOrderByLastNameAscFirstNameAsc()`
  - `PetRepository` - with `findByOwnerOrderByNameAsc(Owner)`
  - `PetTypeRepository` - with `findAllByOrderByNameAsc()`
  - `SpecialtyRepository` - with `findAllByOrderByNameAsc()`
  - `VetRepository` - with `findAllByOrderByLastNameAscFirstNameAsc()`
  - `VisitRepository` - with `findByPetOrderByDateAsc(Pet)`

### Service Layer

- **Replaced** `@Stateless` EJB services with `@Service` + `@Transactional` Spring services
- **Replaced** `@EJB` injection with `@Autowired` constructor injection
- Service interfaces preserved (`OwnerService`, `PetTypeService`, `SpecialtyService`, `VetService`, `VisitService`)
- Service implementations refactored (`*ServiceImpl.java`) to use Spring Data repositories

### REST API Layer

- **Replaced** JAX-RS `@Path` / `@GET` / `@Produces` / `@PathParam` with Spring `@RestController` / `@GetMapping` / `@PathVariable`
- **Replaced** `@Stateless` endpoint EJBs with `@RestController` Spring controllers
- **Preserved** all REST endpoint paths:
  - `GET /rest/owner/list` - list all owners with pets
  - `GET /rest/owner/{id}` - get owner by ID with pets
  - `GET /rest/petType/list` - list all pet types
  - `GET /rest/petType/{id}` - get pet type by ID
  - `GET /rest/specialty/list` - list all specialties
  - `GET /rest/specialty/{id}` - get specialty by ID
  - `GET /rest/vet/list` - list all vets with specialties
  - `GET /rest/vet/{id}` - get vet by ID with specialties
  - `GET /rest/visit/list` - list all visits
  - `GET /rest/visit/{id}` - get visit by ID
- **Added** `GET /actuator/health` and `GET /actuator/info` via Spring Boot Actuator

### DTO / Endpoint Utility Layer

- **Preserved** all DTOs (`OwnerDto`, `PetDto`, `PetTypeDto`, `SpecialtyDto`, `VetDto`, `VisitDto`)
- **Added** missing `email` field to `OwnerDto`
- **Preserved** all `*EndpointUtil.java` converter classes (entity-to-DTO mapping)
- **Replaced** `@RequestScoped` CDI beans with `@Component` Spring beans

### UI Layer

- **Removed** all JSF/PrimeFaces views (`src/main/webapp/` - XHTML templates, beans.xml, faces-config.xml, web.xml)
- **Removed** all `@Named` `@SessionScoped` JSF backing beans (view layer)
- **Removed** i18n resource bundles (`src/main/resources/org/`)
- **Removed** `META-INF/persistence.xml` (replaced by application.properties)

### Server Configuration

- **Removed** `src/main/liberty/` (OpenLiberty server.xml)
- **Removed** `src/main/runtimes/` (multi-runtime configs for Glassfish, Payara, TomEE, Wildfly, Liberty)

### Dockerfile

- **Kept** base image `maven:3.9.12-ibm-semeru-21-noble`
- **Kept** PostgreSQL setup (same user/database/password)
- **Added** `RUN mvn clean package -DskipTests` build step
- **Replaced** `mvn clean liberty:run` with `java -jar target/*.jar`

### Seed Data

- **Preserved** `data.sql` with all original seed data (2 owners, 12 pet types, 5 pets, 7 visits, 7 specialties, 2 vets, 8 vet-specialty associations)
- **Preserved** all sequence restarts (`ALTER SEQUENCE ... RESTART WITH 100`)

### Tests

- **Removed** old Arquillian test infrastructure (`src/test/`)
- **Created** `smoke.sh` - comprehensive shell-based smoke test suite (17 tests)

## Smoke Test Results

All 17 tests passed:
- Actuator health check
- All 5 list endpoints return non-empty JSON arrays
- All 5 single-entity endpoints return correct JSON objects
- Data validation: correct owner data, correct counts for all entity types (12 pet types, 7 specialties, 2 vets, 7 visits)

## Files Summary

| Action | Count | Details |
|--------|-------|---------|
| Created | 41 | 1 main class, 6 entities, 6 repositories, 5 service interfaces, 5 service impls, 5 controllers, 6 DTOs, 6 endpoint utils, 1 application.properties |
| Modified | 2 | pom.xml (replaced), Dockerfile (updated CMD) |
| Removed | ~50+ | JSF views, Liberty config, runtime configs, EJB DAOs, backing beans, persistence.xml, i18n bundles, Arquillian tests |
| Added | 2 | smoke.sh, CHANGELOG.md |
