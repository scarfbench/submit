# CHANGELOG - Quarkus to Jakarta EE (WildFly) Migration

## Overview
Migrated the PetClinic application from Quarkus framework to Jakarta EE running on WildFly 35.0.1.Final (provisioned server via WildFly Maven Plugin 5.x).

## Build System

### pom.xml
- Replaced Quarkus BOM and plugins with Jakarta EE 10 API (`jakarta.jakartaee-api:10.0.0`) as provided dependency
- Added WildFly Maven Plugin (`wildfly-maven-plugin:5.1.2.Final`) with Galleon feature packs:
  - `wildfly-galleon-pack:35.0.1.Final` with `cloud-server` layer
  - `wildfly-datasources-galleon-pack:8.0.1.Final` with `h2-default-datasource` layer
- Added `maven-dependency-plugin` to unpack Bootstrap 5.1.3 and Font Awesome 4.7.0 webjars into static resources (replacing the javax-based `webjars-servlet-2.x` which was incompatible with Jakarta EE)
- Configured `maven-war-plugin` with webResources to include unpacked webjars
- Added JSTL dependencies (`jakarta.servlet.jsp.jstl:3.0.1` impl, `jakarta.servlet.jsp.jstl-api:3.0.2`)
- Added Jackson and JBoss Logging as provided dependencies
- Set `<finalName>ROOT</finalName>` for ROOT context deployment
- Set `<packaging>war</packaging>`

### Dockerfile
- Base image: `maven:3.9.12-ibm-semeru-21-noble`
- Build: `mvn clean package -DskipTests`
- Run: `target/server/bin/standalone.sh -b 0.0.0.0` (WildFly provisioned server)

## Server Configuration

### New Files
- `configure-server.cli` - WildFly CLI script setting `default-web-module=ROOT.war`
- `src/main/resources/META-INF/persistence.xml` - JPA config using `java:jboss/datasources/ExampleDS` (H2), create-drop strategy with `import.sql`
- `src/main/resources/import.sql` - Database seed data (vets, specialties, pet types, owners, pets, visits)
- `src/main/webapp/WEB-INF/web.xml` - Jakarta EE 6.0 web descriptor with welcome file, error page, and JSP encoding config

## Architecture Changes

### HTML Rendering: JAX-RS to Servlets
The original Quarkus app used JAX-RS resources with Qute templates. The initial migration attempted JAX-RS resources with JSP forwarding via `RequestDispatcher`, but this caused Undertow `UT010023` errors (RESTEasy wraps `HttpServletRequest`, and Undertow's `forward()` rejects non-original request wrappers).

**Solution**: Split the architecture:
- **`@WebServlet` classes** handle all HTML-rendering endpoints (JSP forwarding works natively)
- **JAX-RS resources** handle only JSON API endpoints

### New Servlet Classes
- `WelcomeServlet` (`/app`, `/app/`) - forwards to welcome.jsp
- `CrashServlet` (`/app/oups`) - throws RuntimeException for error page testing
- `OwnersServlet` (`/app/owners/*`) - all owner, pet, and visit HTML endpoints with regex-based path routing
- `VetsServlet` (`/app/vets.html`) - veterinarians HTML page

### Simplified JAX-RS Resources
- `JaxRsApplication` - `@ApplicationPath("/api")` (changed from `/app`)
- `VetResource` (`/api/vets`) - JSON-only vet list
- `OwnersResource` (`/api/owners`) - JSON-only owner list
- `ErrorExceptionMapper` - `@Provider` for JAX-RS exception handling, renders inline HTML error page

### Deleted Files
- `WelcomeResource.java` - replaced by WelcomeServlet
- `CrashResource.java` - replaced by CrashServlet
- `PetResource.java` - consolidated into OwnersServlet
- `VisitResource.java` - consolidated into OwnersServlet

## Template Migration: Qute to JSP

### Converted Templates (Qute `.html` to `.jsp`)
- `header.jsp` - navigation bar with JSTL, Bootstrap, Font Awesome
- `footer.jsp` - page footer with Bootstrap JS
- `welcome.jsp` - home page with pets image
- `error.jsp` - error display page
- `findOwners.jsp` - owner search form
- `ownersList.jsp` - owner list table
- `ownerDetails.jsp` - owner detail view with pets and visits
- `createOrUpdateOwnerForm.jsp` - owner create/edit form with validation errors
- `createOrUpdatePetForm.jsp` - pet create/edit form with type dropdown
- `createOrUpdateVisitForm.jsp` - visit create form with pet info and visit history
- `vetList.jsp` - veterinarian list with specialties
- `index.jsp` - redirect to `/app/`

### Key Template Changes
- Replaced Qute `{expression}` syntax with JSTL `<c:out>`, `<c:forEach>`, `<c:if>`, `<c:choose>`
- Replaced Qute `{#include}` with `<jsp:include>`
- All links use `${pageContext.request.contextPath}/app/` prefix
- Static resources served from `/webjars/` and `/css/` paths

## Entity Classes

### Modified for Jakarta EE (from Quarkus Hibernate ORM with Panache)
- `Vet.java` - JPA entity with `@ManyToMany` specialties, `@XmlElement` for JSON
- `Specialty.java` - JPA entity for vet specialties
- `Vets.java` - wrapper class with `@XmlRootElement` for JSON serialization
- `Owner.java` - JPA entity with `@OneToMany` pets, Bean Validation constraints
- `Pet.java` - JPA entity with `@ManyToOne` owner and type
- `PetType.java` - JPA entity for pet types
- `Visit.java` - JPA entity with `petId` foreign key

### Key Changes
- Removed Panache base classes (`PanacheEntity`)
- Added explicit `@Id @GeneratedValue` annotations
- Used `public` fields (matching original Quarkus style)
- Added `@NotBlank` validation constraints on required fields
- Transaction management via `@Inject UserTransaction` (manual begin/commit)

## Static Resources
- `src/main/webapp/css/petclinic.css` - custom styles for navbar, layout, tables
- `src/main/webapp/images/pets.png` - pets image (retained from original)
- `src/main/webapp/images/quarkus-logo.png` - logo (retained from original)

## Smoke Tests
- `smoke.py` - 12 tests covering all endpoints:
  1. Welcome page
  2. Find owners form
  3. List all owners
  4. Owner details
  5. Vets HTML page
  6. Vets JSON API
  7. Owners JSON API
  8. New owner form
  9. Error page
  10. Create new owner (POST)
  11. New pet form
  12. New visit form

**Result: 12/12 tests passing**
