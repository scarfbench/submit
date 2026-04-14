# CHANGELOG: Quarkus to Jakarta EE Migration

## Overview

Migrated the PetClinic application from Quarkus 3.30.5 to Jakarta EE 10 running on WildFly (latest, v39). The application retains all original functionality: owner/pet/visit CRUD, veterinarian listing, error handling, and both HTML and JSON endpoints.

## Migration Summary

| Aspect | Before (Quarkus) | After (Jakarta EE) |
|--------|-------------------|---------------------|
| Runtime | Quarkus 3.30.5 | WildFly 39 (Jakarta EE 10) |
| Build | Quarkus Maven Plugin, JAR | maven-war-plugin, WAR |
| JPA | Hibernate ORM Panache (active record) | Standard JPA EntityManager + JPQL |
| Templating | Quarkus Qute | Thymeleaf 3.1.2 |
| REST | RESTEasy Reactive + Quarkus annotations | Standard JAX-RS (jakarta.ws.rs) |
| CDI | Quarkus CDI (ArC) | Standard Jakarta CDI (Weld) |
| Error Handling | @ServerExceptionMapper | ExceptionMapper<Exception> + @Provider |
| i18n | @MessageBundle / @Localized | Simple Map<String,String> |
| Database | Quarkus DevServices (H2/PostgreSQL) | WildFly H2 Datasource (JNDI) |
| Java Version | 21 | 21 |
| Context Path | / (root) | /petclinic |

## Detailed Changes

### Build System (pom.xml)

- Changed packaging from `jar` to `war`, finalName to `petclinic`
- Removed all Quarkus dependencies: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-rest-qute`, `quarkus-hibernate-orm-panache`, `quarkus-hibernate-validator`, `quarkus-jdbc-h2`, `quarkus-jdbc-postgresql`, `quarkus-container-image-jib`, `quarkus-kubernetes`, `quarkus-web-dependency-locator`, `quarkus-junit5`
- Removed Quarkus BOM (`quarkus-bom`), `quarkus-maven-plugin`, and native profile
- Added: `jakarta.jakartaee-api:10.0.0` (provided), `thymeleaf:3.1.2.RELEASE`, `h2:2.2.224`, `jackson-databind:2.17.0`, `jackson-annotations:2.17.0`
- Added webjars: `bootstrap:5.1.3`, `font-awesome:4.7.0`
- Added `maven-war-plugin:3.4.0` with `failOnMissingWebXml=false`

### Configuration Files

- **Created** `src/main/resources/META-INF/persistence.xml`: JTA persistence unit `petclinicPU` pointing to `java:jboss/datasources/PetClinicDS`, H2 dialect, `create-drop` DDL
- **Created** `src/main/webapp/WEB-INF/beans.xml`: CDI 4.0 descriptor with `bean-discovery-mode="all"`
- **Created** `configure-wildfly.cli`: WildFly CLI script to add H2 datasource
- **Removed** `src/main/resources/application.properties` (Quarkus-specific config)

### Entity Classes

All entity classes updated to use standard JPA instead of Panache:

- **Person.java**: Removed `extends PanacheEntity`, added `@Id @GeneratedValue(strategy=IDENTITY)` field, removed `@FormParam`
- **Owner.java**: Removed Panache static methods (`findByLastName`, `attach`), removed `@FormParam`
- **Pet.java**: Removed `extends PanacheEntity`, added `@Id @GeneratedValue`, removed `attach()` method
- **PetType.java**: Removed `extends PanacheEntity`, `@TemplateExtension`, `parse()` method
- **Visit.java**: Removed `extends PanacheEntity`, `@FormParam`, `findByPetId()` static method
- **Specialty.java**: Removed `extends PanacheEntity`, added `@Id @GeneratedValue`
- **Vet.java**: Inherits standard JPA `@Id` from Person
- **Vets.java**: Added `setVetList()` setter

### Resource Classes (Major Rewrites)

- **OwnersResource.java**: Replaced `TemplatesLocale` with `TemplateEngine` + `EntityManager`. All Panache static calls replaced with JPQL. `@BeanParam Owner` replaced with individual `@FormParam` parameters. Added `@Consumes(APPLICATION_FORM_URLENCODED)` to POST methods. Added `setVisits()` call in `showOwner()`.
- **PetResource.java**: Same pattern. `PetType.parse()` replaced with inline JPQL lookup. `LocalDate` form param handled via `parseDateSafe()` helper.
- **VisitResource.java**: Same pattern. `visit.persist()` replaced with `em.persist(visit)`.
- **VetResource.java**: Replaced `Vet.listAll()` with JPQL query. Returns `String` from TemplateEngine for HTML, `Vets` for JSON.

### System Classes

- **TemplateEngine.java** (Created): `@ApplicationScoped` CDI bean wrapping Thymeleaf engine with programmatic base layout (header/footer HTML). Replaces Qute `{#include base}` fragments.
- **AppMessages.java**: Replaced `@MessageBundle` interface with static `Map<String, String>` containing all 31 message keys.
- **ErrorExceptionMapper.java**: Replaced `@ServerExceptionMapper` with `implements ExceptionMapper<Exception>` + `@Provider`. Replaced `org.jboss.logging.Logger` with `java.util.logging.Logger`.
- **WelcomeResource.java**: Returns `String` instead of `TemplateInstance`.
- **CrashResource.java**: Simplified, removed Quarkus imports.
- **JaxRsApplication.java**: Removed `@Blocking`, added `@ApplicationPath("/")`.
- **Temporals.java**: Removed `@TemplateExtension`.

### Template Migration (Qute to Thymeleaf)

All 10 templates converted from Qute syntax to Thymeleaf:

| Template | Key Changes |
|----------|-------------|
| welcome.html | `{msg:welcome}` -> `th:text="${msg.welcome}"` |
| error.html | Same pattern |
| findOwners.html | `{#for}` -> `th:each`, error display |
| ownersList.html | Owner iteration with `th:each` |
| ownerDetails.html | Owner info + nested pet/visit iteration |
| createOrUpdateOwnerForm.html | Form with conditional error classes |
| createOrUpdatePetForm.html | Pet form with type selector |
| createOrUpdateVisitForm.html | Visit form with pet info table |
| vetList.html | Vet table with specialties |
| base.html | Removed (replaced by programmatic layout in TemplateEngine) |

- All `{#include base}` replaced by programmatic wrapping
- All `{msg:key}` replaced with `th:text="${msg.key}"`
- All `{#for}` / `{#if}` replaced with `th:each` / `th:if`
- Qute `?.` safe navigation replaced with ternary `!= null ? ... : ''`
- All links updated to `/petclinic/` context path

### Static Resources

- **Created** `src/main/webapp/resources/css/petclinic.css`: Compiled from SCSS sources (font-face, navbar, layout, responsive styles)
- Existing images and fonts retained

### Dockerfile

- Multi-stage build: Maven 3.9 (JDK 21) builder + WildFly latest runtime
- Installs python3 for smoke tests
- Copies WAR to WildFly deployments directory
- Runs WildFly CLI to configure H2 datasource
- Fixes directory permissions for jboss user
- Starts WildFly standalone with `-b 0.0.0.0`

### Removed Files

- `src/main/java/.../owner/OwnerForm.java` (no longer needed)
- `src/test/java/.../OwnersResourceTest.java` (Quarkus-specific)
- `src/test/java/.../VetsResourceTest.java` (Quarkus-specific)
- `src/test/java/.../OwnersResourceIT.java`
- `src/test/java/.../VetsResourceIT.java`
- `src/main/resources/application.properties`
- `src/main/resources/templates/base.html`

### Smoke Tests (smoke.py)

Created 13 smoke tests covering all major functionality:

1. Welcome page (GET /)
2. Find owners page (GET /owners/find)
3. Owners list (GET /owners?lastName=)
4. Owner detail (GET /owners/1001)
5. Vets HTML (GET /vets.html)
6. Vets JSON (GET /vets, Accept: application/json)
7. New owner form (GET /owners/new)
8. Create owner (POST /owners/new)
9. Edit owner form (GET /owners/1001/edit)
10. New pet form (GET /owners/1001/pets/new)
11. New visit form (GET /owners/1001/pets/1001/visits/new)
12. Error page (GET /oups)
13. Owners API list JSON (GET /owners/api/list)

## Issues Encountered and Resolved

1. **WildFly Docker image tag**: `quay.io/wildfly/wildfly:31.0.1.Final-jdk21` not found. Resolved by using `quay.io/wildfly/wildfly:latest` (WildFly 39 with JDK 21).
2. **curl-minimal conflict**: RHEL base image has `curl-minimal` which conflicts with `curl` package. Resolved by not installing `curl`.
3. **WildFly CLI empty password**: WildFly rejects empty string passwords. Changed H2 datasource password to `sa`.
4. **WildFly CLI semicolon parsing**: Semicolons in JDBC URLs parsed as CLI command separators. Resolved by quoting the connection-url value.
5. **Directory permissions**: WildFly standalone directory owned by root after CLI configuration. Added `chown -R jboss:jboss` before switching to jboss user.
6. **Thymeleaf OGNL safe navigation**: `pet.type?.name` is not valid OGNL (it's Spring EL). Replaced with `pet.type != null ? pet.type.name : ''`.
7. **Missing setVisits() in showOwner**: The `showOwner` endpoint did not load pet visits. Added `setVisits(owner)` call.

## Test Results

All 13 smoke tests pass:
```
Results: 13 passed, 0 failed out of 13
```
