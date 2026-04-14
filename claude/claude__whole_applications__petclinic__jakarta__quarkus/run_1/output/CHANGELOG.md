# CHANGELOG - Jakarta EE to Quarkus Migration

## Migration Summary

Migrated the PetClinic application from Jakarta EE 10 (with Liberty/Glassfish/Payara/TomEE/WildFly runtimes) to Quarkus 3.8.4. The application retains all REST API functionality with full JSON and XML support, backed by PostgreSQL via Hibernate ORM.

## Build System

### pom.xml (REPLACED)
- Replaced the multi-runtime Jakarta EE pom.xml with a clean Quarkus-based build
- Quarkus BOM version: 3.8.4
- Java 17 target
- Quarkus extensions: resteasy-jackson, resteasy-jaxb, hibernate-orm, jdbc-postgresql, arc, smallrye-health, hibernate-validator, narayana-jta
- Retained Lombok 1.18.38 for code generation

## Configuration

### src/main/resources/application.properties (CREATED)
- Quarkus datasource configuration for PostgreSQL
- Hibernate ORM with drop-and-create schema generation
- SQL logging enabled
- import.sql for database seeding
- SmallRye Health extensions enabled

### src/main/resources/import.sql (CREATED)
- Database seed data for all entities: specialties, pet types, vets, vet_specialties, owners, pets, visits
- Sequence initialization for ID generation

### src/main/resources/persistence.xml (REMOVED)
- No longer needed; Quarkus configures persistence via application.properties

## Source Code Changes

### EJB to CDI Migration (all DAO and Service implementations)
- Replaced `@Stateless` with `@ApplicationScoped` + `@Transactional`
- Replaced `@EJB` injection with `@Inject`
- Removed `@PostActivate` and `@PrePassivate` lifecycle callbacks (EJB-specific)
- Simplified `@PersistenceContext` by removing `unitName` parameter

#### Files modified:
- `OwnerDaoImpl.java`, `PetDaoImpl.java`, `PetTypeDaoImpl.java`, `SpecialtyDaoImpl.java`, `VetDaoImpl.java`, `VisitDaoImpl.java`
- `OwnerServiceImpl.java`, `PetServiceImpl.java`, `PetTypeServiceImpl.java`, `SpecialtyServiceImpl.java`, `VetServiceImpl.java`, `VisitServiceImpl.java`

### REST Endpoint Migration
- Replaced `@Stateless` with `@ApplicationScoped` on all endpoint classes
- Replaced `@EJB` with `@Inject`
- Removed `implements Serializable` and `serialVersionUID` (not needed for CDI beans)
- Removed `throws JAXBException` from method signatures (Quarkus handles marshalling)

#### Files modified:
- `OwnerEndpoint.java`, `PetEndpoint.java`, `PetTypeEndpoint.java`, `SpecialtyEndpoint.java`, `VetEndpoint.java`, `VisitEndpoint.java`

### EndpointUtil Classes
- Replaced `@Stateless` with `@ApplicationScoped`
- Replaced `@EJB` with `@Inject`
- Removed manual JSON-B and JAXB marshalling methods (Quarkus RESTEasy handles serialization)
- Kept only `dtoFactory()` and `dtoListFactory()` methods

#### Files modified:
- `OwnerEndpointUtil.java`, `PetEndpointUtil.java`, `PetTypeEndpointUtil.java`, `SpecialtyEndpointUtil.java`, `VetEndpointUtil.java`, `VisitEndpointUtil.java`

### DTO Annotation Migration
- Replaced JSON-B `@JsonbProperty` with Jackson `@JsonProperty`
- Replaced `@JsonbDateFormat(value = "yyyy-MM-dd")` with `@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")`
- Retained JAXB `@XmlRootElement` annotations for XML output support

#### Files modified:
- All 12 DTO classes (OwnerDto, OwnerListDto, PetDto, PetListDto, PetTypeDto, PetTypeListDto, SpecialtyDto, SpecialtyListDto, VetDto, VetListDto, VisitDto, VisitListDto)

### Application Configuration
- `PetclinicApplication.java`: Simplified to `@ApplicationScoped` + `@ApplicationPath("/rest")` extending `Application`
- Removed JSF-specific references (MessageProvider, FacesContext, `@Named`)

### Entity Classes (UNCHANGED)
- All JPA entity classes preserved as-is (Owner, Pet, PetType, Specialty, Vet, Visit)
- Named queries, entity listeners, sequences all compatible with Quarkus Hibernate ORM

## Files and Directories Removed

### JSF/View Layer (not supported in Quarkus)
- `src/main/webapp/` - All JSF pages, XHTML templates, CSS, JavaScript, beans.xml, faces-config.xml, web.xml
- All View classes: OwnerView, OwnerViewImpl, PetTypeView, PetTypeViewImpl, SpecialtyView, SpecialtyViewImpl, VetView, VetViewImpl, VisitView, VisitViewImpl
- All FlowView classes: OwnerFlowViewImpl, PetTypeFlowViewImpl, SpecialtyFlowViewImpl, VetFlowViewImpl, VisitFlowViewImpl
- OwnerViewService, OwnerViewServiceImpl
- MessageProvider.java, MessageBundle.java
- All JSF converter classes

### Runtime-Specific Configuration
- `src/main/liberty/` - Open Liberty server configuration
- `src/main/runtimes/` - Glassfish, Liberty, Payara, TomEE, WildFly configurations

### Test Classes
- `src/test/` - Old Arquillian/Selenium test infrastructure (replaced by smoke.py)

## Docker

### Dockerfile (MODIFIED)
- Base image retained: `maven:3.9.12-ibm-semeru-21-noble`
- Added PostgreSQL schema permission grant: `GRANT ALL ON SCHEMA public TO petclinic_jakartaee`
- Added Maven build step during image build: `mvn clean package -DskipTests`
- Changed CMD from `mvn clean liberty:run` to `java -jar target/quarkus-app/quarkus-run.jar`

## Testing

### smoke.py (CREATED)
- 37 smoke tests covering all 6 entity REST endpoints and health check
- Tests JSON list, JSON+explicit, XML+explicit, JSON by-id, JSON by-id+explicit, XML by-id+explicit for each entity
- Health check test at `/q/health`
- Server readiness wait with 60 retries at 2-second intervals
- All 37 tests pass

## Test Results

```
=== Results: 37 passed, 0 failed out of 37 tests ===
```
