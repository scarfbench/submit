# CHANGELOG - Jakarta EE to Spring Boot Migration

## Migration Summary

Migrated the Petclinic application from **Jakarta EE 10** (JSF/PrimeFaces, CDI, EJB, JAX-RS, JPA on OpenLiberty) to **Spring Boot 3.2.5** (Spring MVC, Spring Data JPA, Thymeleaf, Jackson, Spring Actuator).

## Actions Performed

### 1. Dependency Migration (pom.xml)

- **Replaced** the entire ~5000-line Jakarta EE pom.xml with a Spring Boot 3.2.5 parent POM
- **Changed** packaging from `war` to `jar`
- **Changed** Java version from 11 to 17
- **Added dependencies**: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-thymeleaf, spring-boot-starter-validation, spring-boot-starter-actuator, postgresql, lombok, jackson-dataformat-xml, webjars (bootstrap 5.2.3, font-awesome 6.4.0, jquery 3.6.0), spring-boot-starter-test
- **Removed**: All Jakarta EE dependencies (JSF, PrimeFaces, CDI, EJB, JAX-RS, JSON-B, JAXB, Liberty, Glassfish, Payara, TomEE, Wildfly runtimes, Arquillian test framework)

### 2. Application Entry Point

- **Created** `PetclinicSpringApplication.java` - Spring Boot main class with `@SpringBootApplication`
- **Created** `application.properties` - Spring Boot configuration (server port 8080, context-path `/petclinic`, PostgreSQL datasource, JPA with Hibernate create DDL, Thymeleaf settings, Actuator endpoints)
- **Created** `data.sql` - Database seed data (migrated from Liberty runtime SQL scripts)

### 3. Entity Layer (Modified)

All 6 JPA entities retained their `jakarta.persistence` annotations (compatible with Spring Boot 3):
- **Owner, Pet, PetType, Vet, Specialty, Visit** - Removed `@EntityListeners` annotations (EntityListener classes deleted)
- **Specialty** - Removed `@XmlElement` JAXB annotation
- Removed all unused imports (EntityListener classes, JAXB)
- Kept `EntityBase` interface and `EntityBaseObject` abstract class (used by all entities)

### 4. Data Access Layer (Replaced)

- **Deleted** all DAO interfaces and implementations (12 files): OwnerDao/Impl, PetDao/Impl, PetTypeDao/Impl, VetDao/Impl, SpecialtyDao/Impl, VisitDao/Impl
- **Created** 6 Spring Data JPA Repository interfaces:
  - `OwnerRepository` - extends JpaRepository with custom search @Query
  - `PetRepository` - extends JpaRepository with owner-based queries
  - `PetTypeRepository` - extends JpaRepository with search @Query
  - `VetRepository` - extends JpaRepository with search @Query
  - `SpecialtyRepository` - extends JpaRepository with search @Query
  - `VisitRepository` - extends JpaRepository with pet-based queries

### 5. Service Layer (Rewritten)

- **Rewritten** 6 service interfaces (simplified, removed CrudService/SearchableService base interfaces)
- **Rewritten** 6 service implementations:
  - Replaced `@Stateless` (EJB) with `@Service` + `@Transactional` (Spring)
  - Replaced `@EJB`/`@Inject` injection with `@Autowired` constructor injection
  - Replaced DAO calls with Spring Data JPA Repository methods
- **Deleted** framework interfaces: `CrudDao`, `CrudService`, `SearchableService`, `SearchableEntity`

### 6. REST API Layer (Rewritten)

- **Rewritten** 6 REST controllers from JAX-RS to Spring:
  - `@Path` -> `@RequestMapping`, `@GET` -> `@GetMapping`, `@PathParam` -> `@PathVariable`
  - OwnerEndpoint, PetEndpoint, PetTypeEndpoint, VetEndpoint, SpecialtyEndpoint, VisitEndpoint
- **Rewritten** 12 DTO classes:
  - Replaced `@JsonbProperty` with `@JsonProperty` (Jackson)
  - Replaced `@JsonbDateFormat` with `@JsonFormat`
  - Removed all `@XmlRootElement` and JAXB annotations
- **Rewritten** 6 EndpointUtil classes:
  - Replaced `@Stateless` with `@Component`
  - Replaced `@EJB` with `@Autowired` constructor injection
  - Removed manual JSON/XML serialization (Jackson handles it in Spring)

### 7. Web UI Layer (Replaced)

- **Deleted** all JSF/PrimeFaces views: 20+ XHTML templates in `src/main/webapp/`
- **Deleted** JSF configuration: `beans.xml`, `faces-config.xml`, `web.xml`
- **Deleted** all JSF managed bean View classes (8 files)
- **Deleted** application configuration: `PetclinicApplication.java`, `MessageBundle.java`, `MessageProvider.java`
- **Created** 5 Spring MVC web controllers: HomeController, OwnerController, VetController, PetTypeController, SpecialtyController
- **Created** 12 Thymeleaf templates: layout.html (shared navbar), home.html, info.html, error.html, owners/list.html, owners/details.html, vets/list.html, vets/details.html, petTypes/list.html, petTypes/details.html, specialties/list.html, specialties/details.html

### 8. Server Configuration (Removed)

- **Deleted** `src/main/liberty/` - OpenLiberty server configuration
- **Deleted** `src/main/runtimes/` - Multi-runtime configurations (Glassfish, Liberty, Payara, TomEE, Wildfly)
- **Deleted** all EntityListener classes (6 files) and EntityListenerLogger

### 9. Test Framework (Replaced)

- **Deleted** all Arquillian-based integration tests
- **Deleted** all deployment descriptors and test suites
- **Created** `smoke.py` - Python-based smoke test suite covering:
  - Actuator health/info endpoints (2 tests)
  - Web UI pages - home, info, owners, vets, petTypes, specialties (6 tests)
  - REST API list endpoints for all 6 entities (6 tests)
  - REST API individual resource endpoints (4 tests)

### 10. Dockerfile (Updated)

- **Changed** build command from `mvn clean liberty:run` to `mvn clean package -DskipTests`
- **Added** build step that starts PostgreSQL, builds the jar, then stops PostgreSQL
- **Changed** runtime command from Liberty server to `java -jar target/petclinic-spring-10.0.0.18-SNAPSHOT.jar`

### 11. Naming Strategy Configuration

- Configured `PhysicalNamingStrategyStandardImpl` to preserve explicit `@Column(name=...)` values from entities
- Configured `ImplicitNamingStrategyLegacyJpaImpl` for JPA-standard implicit naming

## Errors Encountered and Resolutions

### Error 1: Column name mismatch in data.sql

- **Error**: `PSQLException: column "lastname" of relation "owner" does not exist`
- **Cause**: Spring Boot's default `SpringPhysicalNamingStrategy` converts camelCase column names to snake_case (e.g., `lastName` -> `last_name`), but the entity annotations use `@Column(name = "lastName")` and the seed data SQL uses `lastname`
- **Resolution**: Added explicit Hibernate naming strategy configuration in `application.properties` to use `PhysicalNamingStrategyStandardImpl` which preserves column names exactly as specified in `@Column` annotations. PostgreSQL lowercases unquoted identifiers, making `lastName` -> `lastname` which matches the data.sql

## Annotation Mapping Reference

| Jakarta EE | Spring Boot |
|---|---|
| `@Stateless` | `@Service` + `@Transactional` |
| `@Named` | `@Controller` / `@Component` |
| `@EJB` / `@Inject` | `@Autowired` (constructor injection) |
| `@Path` | `@RequestMapping` |
| `@GET` | `@GetMapping` |
| `@PathParam` | `@PathVariable` |
| `@Produces` | `produces=` in mapping |
| `@JsonbProperty` | `@JsonProperty` |
| `@JsonbDateFormat` | `@JsonFormat` |
| `@XmlRootElement` | (removed, Jackson handles serialization) |
| JSF XHTML views | Thymeleaf HTML templates |
| DAO pattern | Spring Data JPA Repository |
| `persistence.xml` | `application.properties` |
| Liberty `server.xml` | `application.properties` |

## Smoke Test Results

```
Results: 18 passed, 0 failed, 18 total
```

All endpoints verified:
- Health actuator returns UP
- All 6 web UI pages load successfully
- All 6 REST list endpoints return valid JSON
- All 4 tested individual resource endpoints return valid JSON
