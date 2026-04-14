# CHANGELOG

## [1.0.0] - Jakarta EE to Spring Boot Migration

### Summary
Complete migration of the RealWorld (Conduit) API from Jakarta EE (MicroProfile/OpenLiberty) to Spring Boot 3.2.5.

### Build & Configuration

- **pom.xml**: Replaced Jakarta EE/MicroProfile dependencies with Spring Boot starters.
  - Parent: `spring-boot-starter-parent:3.2.5`
  - Packaging changed from `war` to `jar`
  - Removed: `microprofile`, `hibernate-core`, `hibernate-validator`, `expressly`, `liberty-maven-plugin`, `weld-junit5`, `microshed-testing`, `rest-assured`, `reflections`, `gson`
  - Added: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-maven-plugin`
  - Retained: `postgresql`, `java-jwt`, `commons-lang3`, `slugify`, `bcrypt`, `jackson-datatype-jsr310`, `lombok`

- **application.properties** (new): Consolidated configuration from `server.xml`, `microprofile-config.properties`, and `persistence.xml` into a single Spring Boot properties file.

- **App.java**: Converted from JAX-RS `Application` subclass to Spring Boot `@SpringBootApplication` main class with `@ComponentScan`, `@EntityScan`, and `@EnableTransactionManagement`.

### Dependency Injection

- Replaced all CDI annotations across 29+ files:
  - `@Inject` -> constructor injection (Spring auto-wiring)
  - `@ApplicationScoped`, `@Singleton`, `@Dependent` -> `@Service`, `@Component`, `@Repository`
  - `@Named` -> `@Component("name")`
  - `@Produces` / CDI qualifiers -> `@Bean` / `@Qualifier`

### REST Layer (JAX-RS -> Spring MVC)

- **ArticlesResource**: Migrated 11 endpoints from JAX-RS (`@Path`, `@GET`, `@POST`, `@PUT`, `@DELETE`) to Spring MVC (`@RestController`, `@RequestMapping`, `@GetMapping`, etc.)
  - `@PathParam` -> `@PathVariable`
  - `@QueryParam` -> `@RequestParam`
  - `@Context SecurityContext` -> `@RequestAttribute("loggedUserId") UUID`
- **UsersResource**: Migrated 2 endpoints (register, login)
- **UserResource**: Migrated 2 endpoints (get current user, update user)
- **ProfilesResource**: Migrated 3 endpoints (get profile, follow, unfollow)
- **TagsResource**: Migrated 1 endpoint (get tags)

### Security

- **AuthenticationFilter**: Converted from JAX-RS `ContainerRequestFilter` with `@NameBinding` to Spring `HandlerInterceptor`.
  - JWT token extraction from `Authorization: Token <jwt>` header
  - Sets `loggedUserId` as request attribute for downstream controllers
  - Supports optional authentication via `@Authenticated(optional = true)`
- **Authenticated** (new annotation): Replaces the old `@Secured`/`@NameBinding` pattern.
- **WebMvcConfig** (new): Registers the `AuthenticationFilter` interceptor and CORS configuration.

### Exception Handling

- **GlobalExceptionHandler** (new `@RestControllerAdvice`): Replaces three JAX-RS `ExceptionMapper` implementations:
  - `InfrastructureExceptionMapper` -> unified handler
  - `BusinessExceptionMapper` -> unified handler
  - `BeanValidationExceptionMapper` -> unified handler
  - Added `HttpMessageNotReadableException` and `MethodArgumentNotValidException` handlers for Spring MVC validation

### Serialization

- **SerializerConfig**: Three-bean `ObjectMapper` strategy:
  - Primary (plain): Used by Spring MVC for request deserialization
  - `wrapRootValueObjectMapper`: For serializing responses with `@JsonRootName` wrapping (e.g., `{"user":{...}}`)
  - `noWrapRootValueObjectMapper`: For list responses without root wrapping (e.g., `{"articles":[...]}`)

### Repository Layer

- All DAO classes (`UserDAO`, `ArticleDAO`, `TagDAO`, `CommentDAO`, etc.) changed from `@Singleton`/`@Dependent` to `@Repository` with constructor injection.
- `@PersistenceContext EntityManager` injection retained (compatible with Spring Boot 3.x/Jakarta Persistence).
- `AbstractDAO` updated to use Spring-managed `EntityManager`.

### Infrastructure Providers

- **JwtTokenProvider**: `@ApplicationScoped` -> `@Component`, `@ConfigProperty` -> `@Value`
- **BCryptHashProvider**: `@ApplicationScoped` -> `@Component`
- **SlugifySlugProvider**: `@ApplicationScoped` -> `@Component`

### Files Deleted

- `src/main/liberty/config/server.xml` (Liberty server configuration)
- `src/main/resources/META-INF/persistence.xml` (replaced by application.properties)
- `src/main/resources/META-INF/beans.xml` (CDI descriptor, not needed in Spring)
- `src/main/resources/META-INF/microprofile-config.properties` (replaced by application.properties)
- `infrastructure/web/security/annotation/Secured.java` (replaced by `@Authenticated`)
- `infrastructure/web/security/context/DecodedJWTSecurityContext.java` (JAX-RS SecurityContext, not needed)
- `infrastructure/web/security/context/EmptySecurityContext.java` (JAX-RS SecurityContext, not needed)
- `infrastructure/web/security/filter/AuthorizationFilter.java` (JAX-RS ContainerRequestFilter, replaced by Spring HandlerInterceptor)
- `infrastructure/web/mapper/InfrastructureExceptionMapper.java` (replaced by GlobalExceptionHandler)
- `infrastructure/web/mapper/BusinessExceptionMapper.java` (replaced by GlobalExceptionHandler)
- `infrastructure/web/mapper/BeanValidationExceptionMapper.java` (replaced by GlobalExceptionHandler)
- `infrastructure/web/qualifiers/NoWrapRootValueObjectMapper.java` (CDI qualifier, replaced by Spring `@Qualifier`)
- Old test files using MicroShed Testing / Weld / Liberty containers

### Deployment

- **start.sh**: Changed from `mvn clean liberty:run` to `mvn clean package -DskipTests && java -jar target/realworld-spring-1.0.0.jar`
- **Dockerfile**: No structural changes needed (Maven + JDK base image works for both Liberty and Spring Boot)

### Smoke Tests

- **smoke.py** (new): 19 Python smoke tests covering all API endpoints:
  - Tags, user registration, login, current user, update user
  - Profiles, follow/unfollow
  - Articles CRUD, feed, list
  - Comments create/list
  - Favorite/unfavorite
  - Unauthorized access validation

### Test Results

All 19 smoke tests passed successfully against the containerized application.
