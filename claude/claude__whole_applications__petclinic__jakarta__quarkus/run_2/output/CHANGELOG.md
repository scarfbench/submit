# CHANGELOG - Jakarta EE to Quarkus Migration

## Migration Summary

Migrated the Petclinic application from Jakarta EE 10 (running on Open Liberty) to Quarkus 3.17.8. The application is a REST API managing veterinary clinic data (owners, pets, visits, vets, specialties, pet types) backed by PostgreSQL.

**Source runtime**: Jakarta EE 10 / MicroProfile 6.0 on Open Liberty (WAR)
**Target runtime**: Quarkus 3.17.8 (JAR)
**Java version**: 21

---

## Build & Configuration Changes

### pom.xml (rewritten)
- Replaced all Jakarta EE / MicroProfile / Liberty Maven profiles and dependencies with Quarkus BOM 3.17.8
- Changed packaging from WAR to JAR
- Key dependencies: `quarkus-rest-jackson`, `quarkus-hibernate-orm`, `quarkus-jdbc-postgresql`, `quarkus-arc`, `quarkus-smallrye-health`, `quarkus-narayana-jta`, `quarkus-hibernate-validator`, `quarkus-jaxb`, `quarkus-rest-jaxb`
- Build plugins: `quarkus-maven-plugin` (build, generate-code, generate-code-tests), `maven-compiler-plugin` with Lombok annotation processor
- Removed all Liberty, WildFly, Payara, GlassFish, and TomEE build profiles

### src/main/resources/application.properties (new)
- Replaces Liberty `server.xml` and `persistence.xml` configurations
- Configures datasource (PostgreSQL), Hibernate ORM (drop-and-create with import.sql), REST path (`/rest`), Jackson serialization, and logging

### src/main/resources/import.sql (new)
- Replaces `META-INF/sql/data.sql` from Liberty runtime
- Contains all seed data: 2 owners, 12 pet types, 5 pets, 7 visits, 7 specialties, 2 vets, vet-specialty associations
- Includes sequence restarts for all entity sequences

### Dockerfile (modified)
- Changed build command from `mvn clean liberty:run` to `mvn clean package -DskipTests`
- Changed runtime command from Liberty to `java -jar target/quarkus-app/quarkus-run.jar`
- Added PostgreSQL schema grant (`GRANT ALL ON SCHEMA public`) for PostgreSQL 15+ compatibility

---

## Code Changes

### DAO Layer (6 files)
Files: `OwnerDaoImpl`, `PetDaoImpl`, `VisitDaoImpl`, `PetTypeDaoImpl`, `VetDaoImpl`, `SpecialtyDaoImpl`

- `@Stateless` -> `@ApplicationScoped`
- `@PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)` -> `@Inject EntityManager`
- Added `@Transactional` to mutating methods (addNew, update, delete)
- Removed EJB lifecycle callbacks (`@PostActivate`, `@PrePassivate`) where present

### Service Layer (7 files)
Files: `OwnerServiceImpl`, `PetServiceImpl`, `VisitServiceImpl`, `PetTypeServiceImpl`, `VetServiceImpl`, `SpecialtyServiceImpl`, `OwnerViewServiceImpl`

- `@Stateless` -> `@ApplicationScoped`
- `@EJB` -> `@Inject`
- Added `@Transactional` to mutating methods

### REST Endpoints (6 files)
Files: `OwnerEndpoint`, `PetEndpoint`, `VisitEndpoint`, `PetTypeEndpoint`, `VetEndpoint`, `SpecialtyEndpoint`

- `@Stateless` -> `@ApplicationScoped`
- `@EJB` -> `@Inject`
- Removed `implements Serializable`
- All JAX-RS annotations preserved (@Path, @GET, @Produces, @PathParam)

### Endpoint Utilities (6 files)
Files: `OwnerEndpointUtil`, `PetEndpointUtil`, `VisitEndpointUtil`, `PetTypeEndpointUtil`, `VetEndpointUtil`, `SpecialtyEndpointUtil`

- `@Stateless` -> `@ApplicationScoped`
- `@EJB` -> `@Inject`
- Removed JAXB/JSON-B manual marshalling helper methods (unnecessary with Quarkus auto-serialization)

### DTOs (12 files)
Files: `OwnerDto`, `OwnerListDto`, `PetDto`, `PetListDto`, `VisitDto`, `VisitListDto`, `PetTypeDto`, `PetTypeListDto`, `VetDto`, `VetListDto`, `SpecialtyDto`, `SpecialtyListDto`

- `@JsonbProperty` -> `@JsonProperty` (Jackson)
- `@JsonbDateFormat(value = "yyyy-MM-dd")` -> `@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")`
- `@NotBlank` on UUID fields -> `@NotNull` (UUID is not a String)

### Application Class
File: `PetclinicApplication.java`

- Removed `extends Application` (Quarkus RESTEasy Reactive disallows @Inject in Application subclasses)
- REST path configured via `quarkus.rest.path=/rest` in application.properties
- Retained as CDI bean for ResourceBundle injection

### View Layer (multiple files)
- `@SessionScoped` -> `@ApplicationScoped`
- `@EJB` -> `@Inject`
- Removed FacesContext, FacesMessage, PrimeFaces DualListModel, SelectItem usage
- PetTypeConverter, SpecialtyConverter: removed @FacesConverter, simplified to CDI beans
- FlashMessagesViewImpl: flash messages converted to log statements
- VetViewImpl: DualListModel replaced with separate source/target lists
- Classes retained as CDI stubs since referenced by service layer

### New Files
- `MessageBundleProducer.java` - CDI producer for ResourceBundle
- `MessageBundle.java` - CDI qualifier annotation
- `smoke.py` - Smoke tests for all REST endpoints

### Removed Files
- All Arquillian/JUnit 4 integration test classes (incompatible with Quarkus)
- Old test configuration files (WEB-INF/beans.xml, faces-config.xml, etc.)

### Unchanged Files
- All JPA entity classes (Jakarta Persistence annotations are compatible)
- All DAO/Service interfaces
- Entity listener classes
- EntityBase, EntityBaseObject, SearchableEntity interfaces

---

## Test Results

All 29 smoke tests passed:
- 6 entity types tested (specialty, petType, vet, owner, pet, visit)
- Endpoint response validation (list, single-item, JSON format)
- Data integrity checks (correct seed data counts)
- Cross-reference checks (owner names, pet types)
- Health endpoint responsive at `/q/health/ready`

---

## Breaking Changes

- JSF/PrimeFaces web UI removed (was at root context). Application is now REST-only.
- WAR deployment no longer supported. Application runs as standalone JAR via `java -jar target/quarkus-app/quarkus-run.jar`.
- Old Arquillian integration tests removed. Replaced by Python smoke tests.
