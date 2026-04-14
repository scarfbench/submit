# CHANGELOG - Quarkus to Jakarta EE Migration

## Migration: Quarkus 3.30.5 -> Jakarta EE 10 (WildFly 35.0.1.Final)

### Build System

- **pom.xml**: Rewrote from Quarkus BOM/plugins to standard Maven WAR packaging
  - Removed all `io.quarkus` dependencies
  - Added `jakarta.jakartaee-api:10.0.0` (provided scope)
  - Added `jackson-databind:2.17.2` for JSON serialization
  - Added `jakarta.servlet.jsp.jstl:3.0.1` for JSTL support
  - Added `h2:2.2.224` for in-memory database
  - Added webjars for Bootstrap 5.1.3 and Font Awesome 4.7.0
  - Changed packaging from `jar` to `war` with `petclinic` finalName
  - Set Java source/target to 21

### Entity Layer

- **Replaced Quarkus Panache with standard JPA**:
  - `model/Person.java`: New `@MappedSuperclass` with id, firstName, lastName fields + JavaBean getters/setters
  - `owner/Owner.java`: Standard `@Entity` extending Person with address, city, telephone, pets + getters/setters
  - `owner/Pet.java`: Standard `@Entity` with `@ManyToOne` type and owner, `@Transient` visits + getters/setters
  - `owner/PetType.java`: Standard `@Entity` with id, name + getters/setters
  - `visit/Visit.java`: Standard `@Entity` with id, date, description, petId + getters/setters
  - `vet/Vet.java`: Standard `@Entity` extending Person with `@ManyToMany` specialties + getters/setters
  - `vet/Specialty.java`: Standard `@Entity` with id, name + getters/setters
  - `vet/Vets.java`: DTO wrapper class with vetList for JSON serialization
  - `vet/SpecialityComparator.java`: Comparator for sorting specialties by name
  - `owner/VisitComparator.java`: Comparator for sorting visits by date

### Controller Layer

- **Replaced Quarkus REST resources with Servlets + JAX-RS**:
  - `owner/OwnersServlet.java`: `@WebServlet("/owners/*")` handling all owner/pet/visit CRUD operations
    - Uses `UserTransaction` for manual transaction management (CDI `@Transactional` not reliable on servlet methods)
    - Regex-based path routing for GET/POST operations
  - `vet/VetServlet.java`: `@WebServlet("/vets.html")` for vet list HTML page
  - `system/WelcomeServlet.java`: `@WebServlet({"", "/"})` for welcome page
  - `system/CrashServlet.java`: `@WebServlet("/oups")` for error page demo
  - `system/WebjarsServlet.java`: `@WebServlet("/webjars/*")` for serving webjar resources from classpath

- **JAX-RS REST API**:
  - `JaxRsApplication.java`: `@ApplicationPath("/api")`
  - `vet/VetResource.java`: `@Path("/vets")` GET returning JSON
  - `owner/OwnersResource.java`: `@Path("/owners")` GET `/list` returning JSON

### Template Layer

- **Replaced Qute templates with JSP + JSTL**:
  - `header.jsp`: Common HTML head, navbar with navigation links
  - `footer.jsp`: Closing HTML tags + Bootstrap JS
  - `welcome.jsp`: Welcome page
  - `error.jsp`: Error page (for `/oups` and general errors)
  - `findOwners.jsp`: Owner search form
  - `ownersList.jsp`: Owner list table with links
  - `ownerDetails.jsp`: Owner detail with pets and visits
  - `createOrUpdateOwnerForm.jsp`: Owner create/edit form
  - `createOrUpdatePetForm.jsp`: Pet create/edit form with pet type selection
  - `createOrUpdateVisitForm.jsp`: Visit create form
  - `vetList.jsp`: Veterinarian list with specialties
  - All JSPs use `fn:escapeXml()` for XSS prevention

### Configuration

- **persistence.xml**: JTA datasource `java:jboss/datasources/PetClinicDS`, Hibernate `create-drop` DDL
- **web.xml**: Jakarta EE 6.0 web-app descriptor
- **beans.xml**: CDI `bean-discovery-mode="all"`
- **wildfly-config.cli**: WildFly CLI script configuring H2 datasource with JNDI

### CDI Beans

- `system/EntityManagerProducer.java`: `@ApplicationScoped` producer for `EntityManager` via `@PersistenceContext`
- `system/DataInitializer.java`: `@Singleton @Startup` bean that populates seed data (10 owners, 13 pets, pet types, vets, specialties, visits)

### Static Resources

- `css/petclinic.css`: Hand-compiled CSS from original SCSS sources
- Copied images and fonts from original resources

### Dockerfile

- Multi-stage build: `maven:3.9.12-eclipse-temurin-21` builder -> `quay.io/wildfly/wildfly:35.0.1.Final-jdk21` runtime
- WildFly CLI configuration for H2 datasource at build time
- Proper permission handling (`chown -R jboss:jboss`) and user switching

### Internationalization

- Replaced Qute `@MessageBundle`/`@Localized` i18n with hardcoded English strings in JSP templates

### Removed Files

- All `src/main/java/org/quarkus/` Quarkus source files
- `src/test/` Quarkus test files (`@QuarkusTest`)
- `src/main/resources/templates/` Qute templates
- `src/main/resources/application.properties`
- `src/main/resources/import.sql`
- `src/main/docker/`, `src/main/kubernetes/`, `src/main/scss/`
- `devfile.yaml`, `db.sh`, `petclinic.sh`, `docker-compose.yml`

### Issues Encountered and Resolved

1. **curl-minimal conflict**: WildFly UBI image has `curl-minimal` pre-installed; removed `curl` from `microdnf install`
2. **Python 3.9 incompatibility**: Removed Python/playwright from Dockerfile; smoke tests use bash/curl
3. **WildFly CLI empty password**: H2 datasource required non-empty password; set `password=sa`
4. **Semicolon in JDBC URL**: H2 URL semicolons parsed as CLI command separators; fixed by quoting the URL
5. **JSP EL property access**: JSP EL requires JavaBean getters; added getter/setter methods to all entity classes
6. **Permission denied on startup**: `USER root` left files owned by root; added `chown -R jboss:jboss` and `USER jboss`
7. **@Transactional on servlets**: CDI interceptors unreliable on servlet lifecycle methods; switched to manual `UserTransaction`

### Smoke Test Results

All 15 tests passing:
- Welcome page (GET /)
- Find owners form (GET /owners/find)
- Owner list (GET /owners?lastName=)
- Owner search (GET /owners?lastName=Davis)
- Create owner form (GET /owners/new)
- Owner detail (GET /owners/1)
- Edit owner form (GET /owners/1/edit)
- Add pet form (GET /owners/1/pets/new)
- Edit pet form (GET /owners/1/pets/1/edit)
- Add visit form (GET /owners/1/pets/1/visits/new)
- Vet list HTML (GET /vets.html)
- Vets JSON API (GET /api/vets)
- Owners JSON API (GET /api/owners/list)
- Error page (GET /oups)
- Create owner POST (POST /owners/new)
