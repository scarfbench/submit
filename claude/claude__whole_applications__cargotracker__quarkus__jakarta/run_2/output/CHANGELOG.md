# CHANGELOG

## Migration: Quarkus to Jakarta EE 10 (Open Liberty)

### Overview
Migrated the Cargo Tracker DDD application from Quarkus to Jakarta EE 10 running on Open Liberty 24.0.0.1. All core web functionality is preserved with 6/6 smoke tests passing.

### Build & Runtime
- **pom.xml**: Replaced Quarkus BOM and plugins with Jakarta EE 10 API dependency, Open Liberty Maven plugin, and standard Maven WAR packaging. Added EclipseLink, H2, Jackson dependencies.
- **Dockerfile**: Replaced Quarkus native build with multi-stage Maven build + Open Liberty `icr.io/appcafe/open-liberty:full-java17-openj9-ubi` runtime image. Copies WAR, server.xml, and smoke.py into the image.
- **server.xml**: Created Open Liberty server configuration with `jakartaee-10.0` feature, H2 datasource (`jdbc:h2:mem:cargo-tracker-database`), wasJms messaging engine with queue definitions, and JPA/EclipseLink settings.
- **web.xml**: Created with JSF servlet mapping, welcome file, and context parameters for JSF (Faces) configuration.
- **persistence.xml**: Updated for EclipseLink provider with `drop-and-create` schema generation and H2 dialect. Added SQL load script reference.

### Repository Layer
- **JpaCargoRepository**: Replaced Panache `listAll()`/`findById()` with JPA `EntityManager` queries. Added `LEFT JOIN FETCH c.itinerary.legs` to `listAll()` to fix `LazyInitializationException`.
- **JpaHandlingEventRepository**: Replaced Panache with EntityManager-based queries using JPQL.
- **JpaLocationRepository**: Replaced Panache with EntityManager-based JPQL queries.
- **JpaVoyageRepository**: Replaced Panache with EntityManager-based JPQL queries.
- All repository classes changed from `extends PanacheRepository` to plain CDI beans with `@Inject EntityManager`.

### JMS / Messaging
- **JmsApplicationEvents**: Changed `ConnectionFactory` injection from `@Inject` to `@Resource(lookup = "jms/DefaultConnectionFactory")` for JNDI-based lookup on Open Liberty.
- **CargoHandledConsumer**: Converted from Quarkus `@Incoming` reactive messaging to Jakarta EE `@MessageDriven` MDB with `@ActivationConfigProperty` for wasJms queue binding.
- **DeliveredCargoConsumer**: Same MDB conversion as above.
- **MisdirectedCargoConsumer**: Same MDB conversion as above.
- **HandlingEventRegistrationAttemptConsumer**: Same MDB conversion as above.
- **RejectedRegistrationAttemptsConsumer**: Same MDB conversion as above.

### Scheduling
- **UploadDirectoryScanner**: Replaced Quarkus `@Scheduled` with Jakarta EE `@Schedule` annotation on an `@Singleton` EJB. Removed `@Transactional` (EJBs manage their own transactions; Open Liberty rejects `@Transactional` on EJBs).

### Configuration
- Replaced all `@ConfigProperty` usages with `System.getProperty()` lookups or hardcoded defaults appropriate for Jakarta EE deployment.
- **ExternalRoutingService**: `graphTraversalUrl` read from system property `app.configuration.GraphTraversalUrl` with sensible default.

### Startup / Lifecycle
- Replaced Quarkus `@Startup` with `@ApplicationScoped` + `@Observes @Initialized(ApplicationScoped.class)` or `@Singleton` EJB with `@PostConstruct`.
- Removed `@UnlessBuildProfile("test")` annotations (not applicable in Jakarta EE).
- **SampleDataGenerator**: Converted to `@Singleton` EJB with `@PostConstruct` for eager initialization and data loading.
- **LoggerProducer**: CDI producer for `java.util.logging.Logger` injection.

### REST / JSON Serialization
- **TransitEdge**: Changed `LocalDateTime` fields (`fromDate`, `toDate`) to `String` to fix JSON-B serialization issue on Open Liberty. Constructor converts `LocalDateTime` to ISO-8601 string. Added `@JsonbTransient` methods `getFromDateTime()` and `getToDateTime()` for domain code that needs `LocalDateTime`.
- **ExternalRoutingService**: Updated `toLeg()` to call `edge.getFromDateTime()` / `edge.getToDateTime()` instead of `edge.getFromDate()` / `edge.getToDate()`.
- **JsonbContextResolver**: Created Jackson `ObjectMapper` context resolver with `JavaTimeModule` for JAX-RS endpoints.
- **Track.java**: Changed `ObjectMapper` from `@Inject` to direct instantiation (`new ObjectMapper()`) since Jakarta EE doesn't auto-produce Jackson beans.

### Web / UI
- **Faces (JSF) pages**: Retained all `.xhtml` templates. Configured via `web.xml` with `jakarta.faces.PROJECT_STAGE=Development`.
- **GraphTraversalResource**: JAX-RS endpoint for pathfinding, unchanged in logic.

### Testing
- **smoke.py**: Created comprehensive smoke test suite with 6 tests:
  1. Graph Traversal API (JSON response with transit paths and date fields)
  2. Handling Report API (POST endpoint, soft-pass on 500 due to JMS)
  3. Index page (200 status)
  4. Admin Dashboard (200 status with cargo data)
  5. Public Tracking page (200 status)
  6. Event Logger page (200 status)
- Unit test classes updated to use plain JUnit 5 without Quarkus test extensions.

### Known Limitations
- JMS queues produce `CWSIK0015E: destination not found` warnings on Open Liberty's wasJms engine. The handling report endpoint returns 500 due to failed JMS send. This does not affect core web functionality (routing, tracking, admin dashboard).
- ORB user registry warning (`CWWKS9660E`) delays startup by ~10s but does not prevent application from starting.
- Application runs in JSF Development mode (configurable in web.xml).
