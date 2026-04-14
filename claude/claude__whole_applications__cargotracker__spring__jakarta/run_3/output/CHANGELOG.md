# Migration Changelog - Spring to Jakarta EE

## [2026-03-13T17:00:00Z] [info] Project Analysis
- Identified Eclipse Cargo Tracker application with 90+ Java source files
- Build system: Maven with WAR packaging
- Application server: Payara Micro 6.2024.6 (Jakarta EE 10 compatible)
- Codebase already uses Jakarta EE 10 API (`jakarta.jakartaee-api:10.0.0`)
- All Java imports use `jakarta.*` namespace (no `javax.*` or `org.springframework.*` references found)
- No Spring Boot, Spring MVC, or other Spring dependencies present
- Uses Jakarta CDI, JPA, JAX-RS, JSF (Faces), Batch, EJB, and Bean Validation
- Frontend: Jakarta Server Faces (JSF) with PrimeFaces 14.0.0 (Jakarta classifier)
- Database: H2 in-memory with JPA/EclipseLink
- Architecture: Domain-Driven Design (DDD) with clear bounded contexts

## [2026-03-13T17:01:00Z] [info] Initial Build Attempt
- Docker image built successfully from existing Dockerfile
- Maven compilation succeeded (`mvn clean package -DskipTests`)
- WAR file produced at `target/cargo-tracker.war`

## [2026-03-13T17:02:00Z] [error] Application Deployment Failure
- Container started but Payara Micro failed to deploy the WAR
- Root cause: `java.lang.IllegalArgumentException: NamedQuery of name: Cargo.findByTrackingId not found`
- Secondary error: `jakarta.ejb.CreateException: Initialization failed for Singleton SampleDataGenerator`
- The `SampleDataGenerator` @Startup EJB singleton failed during `@PostConstruct` because
  the JPA named queries referenced in repository classes were not defined on entity classes
- Schema generation (`drop-and-create`) produced expected warnings about non-existent tables
  during the DROP phase (harmless on first run with empty in-memory H2 database)

## [2026-03-13T17:03:00Z] [error] Missing @NamedQuery Annotations Identified
- File: `src/main/java/org/eclipse/cargotracker/domain/model/cargo/Cargo.java`
  - Issue: Entity class missing `@NamedQuery` annotation for `"Cargo.findByTrackingId"`
  - Used by: `JpaCargoRepository.findByTrackingId()` via `createNamedQuery("Cargo.findByTrackingId")`
- File: `src/main/java/org/eclipse/cargotracker/domain/model/location/Location.java`
  - Issue: Entity class missing `@NamedQuery` annotation for `"Location.findByUnLocode"`
  - Used by: `JpaLocationRepository.findByUnLocode()` via `createNamedQuery("Location.findByUnLocode")`
- Other entities (`Voyage.java`, `HandlingEvent.java`) already had correct `@NamedQuery` annotations

## [2026-03-13T17:04:00Z] [info] Fix Applied - Cargo.java
- Added `import jakarta.persistence.NamedQuery`
- Added `@NamedQuery(name = "Cargo.findByTrackingId", query = "Select c from Cargo c where c.trackingId = :trackingId")`
- Added `@NamedQuery(name = "Cargo.findAll", query = "Select c from Cargo c order by c.trackingId")`

## [2026-03-13T17:04:30Z] [info] Fix Applied - Location.java
- Added `import jakarta.persistence.NamedQuery`
- Added `@NamedQuery(name = "Location.findByUnLocode", query = "Select l from Location l where l.unLocode = :unLocode")`
- Added `@NamedQuery(name = "Location.findAll", query = "Select l from Location l order by l.name")`

## [2026-03-13T17:05:00Z] [info] Rebuild and Retest
- Docker image rebuilt successfully
- Maven compilation succeeded
- Payara Micro deployed WAR successfully with context root `/cargo-tracker`
- All REST endpoints registered:
  - `GET /cargo-tracker/rest/cargo` (SSE)
  - `GET /cargo-tracker/rest/graph-traversal/shortest-path`
  - `POST /cargo-tracker/rest/handling/reports`
- Sample data loaded successfully (locations, voyages, cargos, handling events)

## [2026-03-13T17:06:00Z] [info] Smoke Tests - All Passed (7/7)
- `test_root_page` - PASS: Root page loads (HTTP 200)
- `test_admin_dashboard` - PASS: Admin dashboard renders (HTTP 200)
- `test_graph_traversal_rest` - PASS: REST API returns transit paths with edges
- `test_handling_report_endpoint` - PASS: POST handling report accepted (HTTP 200/204)
- `test_cargo_sse_endpoint` - PASS: SSE endpoint streams cargo data
- `test_tracking_page` - PASS: Tracking page loads (HTTP 200)
- `test_event_logger_page` - PASS: Event logger page loads (HTTP 200)

## [2026-03-13T17:07:00Z] [info] Migration Complete
- All source code uses Jakarta EE 10 APIs exclusively
- No Spring Framework dependencies remain
- Application builds, deploys, and passes all smoke tests
- Dynamic port allocation used for Docker (assigned port varies per run)
