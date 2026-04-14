# Changelog

## Migration: Jakarta EE / OpenLiberty → Spring Boot

### Overview
Migrated the RealWorld API application from Jakarta EE (JAX-RS, CDI, MicroProfile) running on OpenLiberty to Spring Boot 3.4.1 with embedded Tomcat.

### Build & Packaging

- **pom.xml**: Replaced Jakarta EE / MicroProfile / Liberty Maven dependencies with Spring Boot parent (`spring-boot-starter-parent:3.4.1`) and starters (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`). Changed packaging from `war` to `jar`. Replaced `liberty-maven-plugin` with `spring-boot-maven-plugin`.
- **start.sh**: Changed from `mvn clean liberty:run` to `mvn clean package -DskipTests -q && java -jar target/realworld-spring.jar`.

### Application Entry Point

- **App.java**: Converted from JAX-RS `Application` with `@ApplicationPath("api")` to `@SpringBootApplication` with `@ComponentScan`, `@EntityScan`, and `@EnableTransactionManagement`.

### Configuration

- **Created `application.properties`**: Consolidated configuration from `persistence.xml`, `microprofile-config.properties`, and `server.xml` into a single Spring Boot properties file. Includes datasource, JPA/Hibernate, JWT, Jackson, and Actuator settings.
- **Removed**: `persistence.xml`, `microprofile-config.properties`, `beans.xml`, `src/main/liberty/config/server.xml`.

### REST Controllers (JAX-RS → Spring MVC)

All 5 resource classes migrated:
- `ArticlesResource`: `@Path` → `@RequestMapping`, `@GET/@POST/@PUT/@DELETE` → `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping`, `@PathParam` → `@PathVariable`, `@QueryParam` → `@RequestParam`, `Response` → `ResponseEntity`
- `UsersResource`: Same annotation migration plus `@Transactional` for state-changing operations
- `UserResource`: Same pattern
- `ProfilesResource`: Same pattern
- `TagsResource`: Same pattern

### Security / Authentication

- **AuthenticationFilter**: Converted from JAX-RS `ContainerRequestFilter` to Spring `HandlerInterceptor`. Sets `loggedUserId` as a request attribute instead of using `SecurityContext`.
- **Created `WebConfig`**: Registers the authentication interceptor for `/api/**` paths and configures CORS.
- **Removed**: `AuthorizationFilter.java` (JAX-RS filter), `DecodedJWTSecurityContext.java`, `EmptySecurityContext.java`, `Secured.java` (custom `@NameBinding` annotation).

### Dependency Injection (CDI → Spring)

- All `@ApplicationScoped` / `@Singleton` / `@Dependent` → `@Component`, `@Service`, or `@Repository` as appropriate
- All `@Inject` → `@Autowired`
- CDI `@Produces` methods → Spring `@Bean` in `@Configuration` classes
- MicroProfile `@ConfigProperty` → Spring `@Value`

### Exception Handling

- Replaced 3 JAX-RS `ExceptionMapper` classes with a single `@RestControllerAdvice` `GlobalExceptionHandler`
- Maps domain exceptions to appropriate HTTP status codes (401, 403, 404, 409, 422)
- **Removed**: `BusinessExceptionMapper.java`, `InfrastructureExceptionMapper.java` (merged into `GlobalExceptionHandler`)

### Repository Layer

- 7 DAO classes: CDI scopes → `@Repository`, `@Inject` → `@Autowired`
- JPA `EntityManager` usage preserved (works identically in Spring)
- `@PersistenceContext` annotation unchanged

### Domain Layer

- 4 model builder classes: `@Singleton` → `@Component`
- `ModelValidator`: `@Singleton` → `@Component`
- 29 feature implementations: `@Singleton`/`@Dependent` → `@Component`

### Jackson / Serialization

- **SerializerConfig**: Replaced CDI `@Produces` with Spring `@Bean`/`@Configuration`. Removed `WRAP_ROOT_VALUE`/`UNWRAP_ROOT_VALUE` from global ObjectMapper since the application uses explicit wrapper classes and `Collections.singletonMap` for root-level JSON wrapping.
- Controllers wrap single-entity responses using `Collections.singletonMap("key", response)` for proper RealWorld API JSON format (e.g., `{"user": {...}}`, `{"article": {...}}`).
- **Removed**: `NoWrapRootValueObjectMapper.java` (CDI qualifier annotation, no longer needed).

### Request Models

- Removed `jakarta.json.bind.annotation.JsonbNillable` imports from 3 request model files (Spring uses Jackson, not JSON-B).

### Infrastructure Providers

- `JwtTokenProvider`: `@ApplicationScoped` → `@Component`, `@ConfigProperty` → `@Value`
- `BCryptHashProvider`: `@ApplicationScoped` → `@Component`
- `SlugifySlugProvider`: `@ApplicationScoped` → `@Component`
- `EntityUtils`: `@ApplicationScoped` → `@Component`

### Testing

- **Created `smoke.py`**: Comprehensive smoke test with 23 test cases covering health check, user registration/login, articles CRUD, comments, favorites, profiles/follow, tags, and error cases.
- **Removed**: Old Liberty/MicroShed test files in `src/test/`.

### Files Removed

- `src/main/java/org/example/realworldapi/infrastructure/web/security/filter/AuthorizationFilter.java`
- `src/main/java/org/example/realworldapi/infrastructure/web/security/context/DecodedJWTSecurityContext.java`
- `src/main/java/org/example/realworldapi/infrastructure/web/security/context/EmptySecurityContext.java`
- `src/main/java/org/example/realworldapi/infrastructure/web/security/annotation/Secured.java`
- `src/main/java/org/example/realworldapi/infrastructure/web/qualifiers/NoWrapRootValueObjectMapper.java`
- `src/main/java/org/example/realworldapi/infrastructure/web/mapper/BusinessExceptionMapper.java`
- `src/main/java/org/example/realworldapi/infrastructure/web/mapper/InfrastructureExceptionMapper.java`
- `src/main/resources/META-INF/persistence.xml`
- `src/main/resources/META-INF/microprofile-config.properties`
- `src/main/resources/META-INF/beans.xml`
- `src/main/liberty/config/server.xml`
- `src/test/` (entire directory)

### Files Created

- `src/main/resources/application.properties`
- `src/main/java/org/example/realworldapi/infrastructure/config/WebConfig.java`
- `smoke.py`

### Smoke Test Results

All 23 tests passing:
- Health check, tags, articles listing
- User registration, login, profile retrieval and update
- Article CRUD (create, read, update, delete)
- Article favorites/unfavorites
- Comments (create, list)
- User profiles, follow/unfollow
- Error handling (wrong password → 401, duplicate registration → 409)
