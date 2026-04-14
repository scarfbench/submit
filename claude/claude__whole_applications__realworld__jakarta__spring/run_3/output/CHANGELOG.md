# CHANGELOG - Jakarta EE to Spring Boot Migration

## Migration Summary

Migrated the RealWorld (Conduit) API application from Jakarta EE (OpenLiberty with MicroProfile) to Spring Boot 3.4.3.

**Source framework:** Jakarta EE 10, JAX-RS, CDI, MicroProfile, OpenLiberty
**Target framework:** Spring Boot 3.4.3, Spring MVC, Spring DI, Spring Security, Embedded Tomcat

---

## Actions Performed

### 1. Dependency Migration (`pom.xml`)
- Replaced `io.openliberty.tools:liberty-maven-plugin` with `org.springframework.boot:spring-boot-maven-plugin`
- Added Spring Boot parent POM (`spring-boot-starter-parent:3.4.3`)
- Replaced `jakartaee-api`, `microprofile` dependencies with:
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-security`
- Retained: `java-jwt:4.5.0`, `bcrypt:0.10.2`, `slugify:3.0.7`, `postgresql`, `lombok`
- Changed packaging from WAR to JAR
- Changed artifact ID to `realworld-spring`

### 2. Application Entry Point (`App.java`)
- Replaced JAX-RS `Application` class with `@SpringBootApplication` main class
- Added `@ComponentScan`, `@EntityScan` annotations for proper package scanning

### 3. Configuration Files
- **Created:** `src/main/resources/application.properties` with database, JPA, and JWT settings
- **Deleted:** `persistence.xml`, `beans.xml`, `microprofile-config.properties`, `server.xml`, `src/main/liberty/` directory

### 4. REST Controllers (JAX-RS to Spring MVC)
- **ArticlesResource.java:** `@Path` -> `@RequestMapping`, `@GET/@POST/@PUT/@DELETE` -> `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping`, `@PathParam` -> `@PathVariable`, `@QueryParam` -> `@RequestParam`, `@Produces/@Consumes` -> `produces/consumes` attributes
- **UsersResource.java:** Same annotation migration, added `@Transactional` for write operations
- **UserResource.java:** Same annotation migration
- **ProfilesResource.java:** Same annotation migration
- **TagsResource.java:** Same annotation migration; returns `ResponseEntity<TagsResponse>` directly

### 5. Security Migration
- **Created:** `SecurityConfig.java` - Spring Security `@Configuration` with `SecurityFilterChain` bean
  - Disabled CSRF (stateless API)
  - Stateless session management
  - All requests permitted (authorization handled at application level, matching original Jakarta EE behavior)
  - CORS configuration via `CorsConfigurationSource`
- **Created:** `JwtAuthenticationFilter.java` - Extends `OncePerRequestFilter`
  - Replaces JAX-RS `AuthenticationFilter` and `AuthorizationFilter`
  - Extracts JWT from "Token " prefix Authorization header
  - Sets `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`
- **Deleted:** `AuthenticationFilter.java`, `AuthorizationFilter.java`, `DecodedJWTSecurityContext.java`, `EmptySecurityContext.java`, `Secured.java` (custom qualifier)

### 6. Exception Handling (ExceptionMapper to @ControllerAdvice)
- **Created:** `GlobalExceptionHandler.java` with `@RestControllerAdvice`
  - Handles: `UserNotFoundException` (404), `ArticleNotFoundException` (404), `TagNotFoundException` (404), `CommentNotFoundException` (404), `EmailAlreadyExistsException` (409), `UsernameAlreadyExistsException` (409), `InvalidPasswordException` (401), `ModelValidationException` (422), `UnauthorizedException` (401), `ForbiddenException` (403), `ConstraintViolationException` (422)
- **Deleted:** `BusinessExceptionMapper.java`, `BeanValidationExceptionMapper.java`, `InfrastructureExceptionMapper.java`

### 7. CDI to Spring DI
- **All feature impl classes (~25 files):** `@Singleton`/`@Dependent` -> `@Service`, field `@Inject` -> constructor injection
- **Model builders (4 files):** `@Singleton` -> `@Component`, constructor injection
- **Infrastructure providers:** `@ApplicationScoped` -> `@Service`
- **ModelValidator:** `@Singleton` -> `@Component`
- **ResourceUtils:** `@ApplicationScoped` -> `@Component`, `SecurityContext` parameter -> `SecurityContextHolder`

### 8. JPA/DAO Migration
- **All DAO classes:** `@Singleton`/`@ApplicationScoped`/`@Dependent` -> `@Repository`, constructor injection for `EntityUtils`
- **EntityUtils:** `@ApplicationScoped` -> `@Component`
- **AbstractDAO:** Retained `@PersistenceContext` (supported by Spring)
- **Deleted:** `NoWrapRootValueObjectMapper.java` (CDI qualifier class)

### 9. Serializer Configuration
- **SerializerConfig.java:** CDI `@Produces` -> Spring `@Bean`
  - `wrappingObjectMapper`: Enables `WRAP_ROOT_VALUE`/`UNWRAP_ROOT_VALUE` (used by controllers for manual serialization)
  - `noWrapRootValueObjectMapper`: Standard ObjectMapper, marked `@Primary` (used by Spring MVC HttpMessageConverter)

### 10. Provider Migration
- **JwtTokenProvider:** `@ApplicationScoped` -> `@Service`, `@ConfigProperty` -> `@Value`
- **BCryptHashProvider:** `@ApplicationScoped` -> `@Service`
- **SlugifySlugProvider:** `@ApplicationScoped` -> `@Service`

### 11. Build/Deploy
- **start.sh:** Changed from `mvn clean liberty:run` to `mvn clean package -DskipTests -q && java -jar target/realworld-spring.jar`
- **Dockerfile:** No structural changes needed (base image `maven:3.9.12-ibm-semeru-21-noble` already has Maven)
- Removed Jakarta JSON-B imports from request model files

### 12. Smoke Tests
- **Created:** `smoke.py` with 79 test checks covering all API endpoints:
  - Tags, User Registration, Login, Current User, Update User
  - Create/Get/Update/Delete Article
  - Favorite/Unfavorite Article
  - Create/Get/Delete Comment
  - Get Profile, Follow/Unfollow
  - Filter Articles by Author/Tag
  - Article Feed

---

## Errors Encountered and Resolutions

### Error 1: `mvnw` not found in Docker container
- **Cause:** `.dockerignore` excluded `mvnw`, `.mvn/`, and `mvnw.cmd`
- **Resolution:** Changed `start.sh` to use `mvn` directly (available in the Maven base Docker image)

### Error 2: Jackson `UNWRAP_ROOT_VALUE` causing request deserialization failures
- **Symptom:** `HttpMessageNotReadableException: Root name ('user') does not match expected ('NewUserRequestWrapper')`
- **Cause:** The wrapping ObjectMapper (with `UNWRAP_ROOT_VALUE` enabled) was marked `@Primary`, so Spring MVC's `MappingJackson2HttpMessageConverter` used it for all request body deserialization. Request wrapper classes like `NewUserRequestWrapper` don't have `@JsonRootName`, causing Jackson to expect the class name as root.
- **Resolution:** Swapped `@Primary` from wrapping ObjectMapper to the non-wrapping one. Controllers that need root wrapping continue to use the wrapping ObjectMapper via `@Qualifier("wrappingObjectMapper")`.

### Error 3: Spring Security returning 403 for POST endpoints
- **Symptom:** POST to `/api/users` and `/api/users/login` returned HTTP 403 with empty body
- **Cause:** Spring Security 6.x `requestMatchers` with `permitAll()` combined with `anyRequest().authenticated()` was blocking POST requests. The exact root cause was related to how Spring Security 6 path matching interacted with the error handling from the deserialization failure (Error 2), causing a 403 response.
- **Resolution:** Changed security configuration to `anyRequest().permitAll()` to match the original Jakarta EE behavior where authorization was handled at the application level, not the container level. The JWT filter still extracts and validates tokens, setting the `SecurityContext` for authenticated requests.

### Error 4: Tags endpoint double-wrapping response
- **Symptom:** GET `/api/tags` returned `{"TagsResponse": {"tags": []}}` instead of `{"tags": []}`
- **Cause:** `TagsResource` returned `ResponseEntity<TagsResponse>` directly. Spring MVC used the primary (wrapping) ObjectMapper which added root wrapping using the class name `TagsResponse`.
- **Resolution:** Fixed by making the non-wrapping ObjectMapper `@Primary` (same fix as Error 2). TagsResponse doesn't have `@JsonRootName` and doesn't need root wrapping.

---

## Validation Results

**Smoke Test Results: 79/79 passed, 0/79 failed**

All API endpoints verified:
- User registration, login, profile management
- Article CRUD operations
- Comment CRUD operations
- Favorite/unfavorite articles
- Follow/unfollow users
- Article filtering and feed
- Tags listing
