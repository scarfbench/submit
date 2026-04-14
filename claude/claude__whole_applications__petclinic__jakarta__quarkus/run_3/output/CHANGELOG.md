# CHANGELOG - Jakarta EE 10 to Quarkus 3.8.4 Migration

## Summary

Migrated the PetClinic application from Jakarta EE 10 (OpenLiberty) to Quarkus 3.8.4. The application is a REST API managing veterinary clinic data (owners, pets, pet types, specialties, vets, visits) backed by PostgreSQL.

## Build System

- **pom.xml**: Complete rewrite from Jakarta EE/Liberty WAR packaging to Quarkus JAR packaging
  - Replaced all Jakarta EE / Liberty dependencies with Quarkus equivalents
  - Changed packaging from `war` to `jar`
  - Set Java target to 17, Quarkus BOM 3.8.4
  - Dependencies: `quarkus-resteasy-reactive-jackson`, `quarkus-hibernate-orm`, `quarkus-jdbc-postgresql`, `quarkus-arc`, `quarkus-smallrye-health`, `quarkus-hibernate-validator`
  - Retained Lombok with annotation processor configuration
  - Replaced Arquillian test dependencies with `quarkus-junit5` and `rest-assured`

## Configuration

- **Removed**: `src/main/liberty/` (server.xml, Liberty server config)
- **Removed**: `src/main/runtimes/` (all runtime-specific configurations)
- **Added**: `src/main/resources/application.properties` (Quarkus datasource, Hibernate ORM, HTTP, Jackson config)
- **Added**: `src/main/resources/import.sql` (seed data, migrated from data.sql)
- **Removed**: `src/main/resources/META-INF/persistence.xml` (Quarkus auto-configures Hibernate)

## CDI / EJB Migration

- All `@Stateless` EJB annotations replaced with `@ApplicationScoped` (CDI)
- All `@EJB` injection replaced with `@Inject`
- All `@PersistenceContext(unitName="petclinicPersistenceUnit")` replaced with `@Inject EntityManager`
- Added `@Transactional` (jakarta.transaction) on all data-mutating DAO and Service methods
- Removed `Serializable` from non-entity classes (DAOs, Services, Endpoints, EndpointUtils)

## Entity Classes

- Preserved all JPA entity annotations unchanged (entities remain compatible)
- Preserved all entity listeners (`@PrePersist`, `@PreUpdate`, `@PreRemove`)
- Preserved all named queries
- Preserved `Serializable` on entity classes (JPA requirement)
- Files: Owner, Pet (OwnerPet), PetType (OwnerPetPetType), Specialty, Vet, Visit (OwnerPetVisit)

## DAO Layer

- `CrudDao` interface: Removed `PERSISTENCE_UNIT_NAME` constant, removed `SearchableEntity` references
- All `*DaoImpl` classes: `@Stateless` -> `@ApplicationScoped`, `@PersistenceContext` -> `@Inject EntityManager`, added `@Transactional` on mutating methods

## Service Layer

- `CrudService` interface: Removed `SearchableService` references, removed `Serializable`
- All `*ServiceImpl` classes: `@Stateless` -> `@ApplicationScoped`, `@EJB` -> `@Inject`, added `@Transactional` on mutating methods

## REST / Serialization

- **Endpoints**: `@Stateless` -> `@ApplicationScoped`, `@EJB` -> `@Inject`, added class-level `@Produces(MediaType.APPLICATION_JSON)`, removed `throws JAXBException`
- **DTOs**: Replaced JSON-B annotations (`@JsonbProperty`, `@JsonbDateFormat`) with Jackson annotations (`@JsonProperty`, `@JsonFormat`), removed JAXB annotations (`@XmlRootElement`), added `@JsonInclude(JsonInclude.Include.NON_NULL)`
- **EndpointUtil classes**: `@Stateless` -> `@ApplicationScoped`, `@EJB` -> `@Inject`, removed manual JSON-B/JAXB marshalling methods (Jackson handles serialization automatically via RESTEasy Reactive)
- **PetclinicApplication**: Simplified to `@ApplicationPath("/rest")` extending `Application` only

## Removed Files (JSF / Liberty specific)

- All JSF view classes (`*View.java`, `*FlowView.java`)
- All JSF converters (`PetTypeConverter.java`, `SpecialtyConverter.java`)
- All JSF templates and pages (`src/main/webapp/`)
- Message bundle classes (`MessageBundle.java`, `MessageProvider.java`)
- Liberty-specific interfaces (`SearchableEntity.java`, `SearchableService.java`)
- Utility class `EntityListenerLogger.java`
- Owner view service (`OwnerViewService.java`, `OwnerViewServiceImpl.java`)
- Old test directory (`src/test/`)
- All `views/` subdirectories

## Dockerfile

- Replaced Liberty-based runtime with Quarkus fast-jar
- Added Maven build step during Docker image build: `mvn clean package -DskipTests`
- Changed CMD from `mvn clean liberty:run` to `java -jar target/quarkus-app/quarkus-run.jar`
- PostgreSQL setup retained for local database

## Smoke Tests

- Created `smoke.py` with 23 tests covering all REST endpoints
- Tests verify: Owner, Pet, PetType, Specialty, Vet, Visit list/detail endpoints
- Tests verify: JSON response format, data integrity (seed data counts)
- Health check endpoint: `/q/health/ready`

## Test Results

All 23 smoke tests passed:
- 6 resource types x 3 endpoint patterns (list, detail by id, list+json) = 18 endpoint tests
- 1 additional owner detail check (specific field value)
- 4 data integrity checks (owner count >= 2, pet types >= 12, specialties >= 7, vets >= 2)

## Errors Fixed During Migration

1. **Import typo in PetTypeEndpoint.java**: `org.woehlke.jakartaee.pettype.api.PetTypeListDto` was missing `.petclinic.` segment. Fixed to `org.woehlke.jakartaee.petclinic.pettype.api.PetTypeListDto`.
2. **Uber-jar configuration**: The `-Dquarkus.package.jar.type=uber-jar` Maven system property was not being applied in Quarkus 3.8.4. Switched to default fast-jar format (`target/quarkus-app/quarkus-run.jar`).
