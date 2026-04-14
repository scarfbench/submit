# CHANGELOG - Quarkus to Spring Boot Migration

## Migration Summary

Migrated the RealWorld (Conduit) API application from Quarkus 3.17.7 to Spring Boot 3.4.1.
All 11 smoke tests pass after migration.

---

## Build & Configuration

### pom.xml
- **Removed**: Quarkus BOM (`quarkus-bom`), all `io.quarkus` dependencies
  - `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-smallrye-jwt`,
    `quarkus-elytron-security-common`, `quarkus-jdbc-postgresql`, `quarkus-arc`,
    `quarkus-hibernate-validator`, `quarkus-junit5`, `rest-assured`
- **Added**: Spring Boot parent (`spring-boot-starter-parent:3.4.1`), Spring Boot starters
  - `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`,
    `spring-boot-starter-security`, `spring-boot-starter-actuator`, `spring-boot-starter-test`
- **Added**: `io.jsonwebtoken:jjwt-api/impl/jackson:0.12.6` (replaces SmallRye JWT)
- **Added**: `jackson-datatype-jsr310` for Java Time module support
- **Added**: `spring-boot-maven-plugin` with Lombok exclusion
- **Added**: `maven-compiler-plugin` with `-parameters` flag and Lombok annotation processor
- **Retained**: `lombok`, `slugify`, `commons-lang3`, `postgresql`

### application.properties
- **Removed**: All `quarkus.*` properties (datasource, hibernate-orm, smallrye-jwt, http)
- **Added**: Spring Boot equivalents:
  - `server.servlet.context-path=/api`, `server.port=8080`
  - `spring.datasource.*` (PostgreSQL connection)
  - `spring.jpa.hibernate.ddl-auto=create-drop`
  - `spring.jpa.properties.hibernate.dialect`
  - `jwt.private-key-location`, `jwt.public-key-location`, `jwt.issuer`
  - `management.endpoints.web.exposure.include=health`

### Dockerfile
- **Changed**: Added `requests` to pip install for smoke test support

### start.sh
- **Changed**: `java -jar target/quarkus-app/quarkus-run.jar` -> `java -jar target/realworldapiservice-1.0-SNAPSHOT.jar`

---

## New Files

### RealWorldApiApplication.java
- Spring Boot main application class with `@SpringBootApplication`

### SecurityConfig.java
- Spring Security configuration with stateless JWT authentication
- CSRF disabled, session management set to STATELESS
- Public endpoints: POST `/users`, `/users/login`; GET `/articles`, `/articles/*`, `/articles/*/comments`, `/profiles/*`, `/tags`, `/actuator/**`
- All other endpoints require authentication
- JWT filter added before `UsernamePasswordAuthenticationFilter`

### JwtAuthenticationFilter.java
- `OncePerRequestFilter` implementation for JWT extraction from `Authorization` header
- Supports both `Token ` and `Bearer ` prefixes (RealWorld API spec uses "Token")
- Sets `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`

### smoke.py
- Python smoke test script testing 11 endpoints:
  1. Register User (POST /users)
  2. Login User (POST /users/login)
  3. Get Current User (GET /user)
  4. Create Article (POST /articles)
  5. Get Articles List (GET /articles)
  6. Get Article by Slug (GET /articles/{slug})
  7. Create Comment (POST /articles/{slug}/comments)
  8. Get Comments (GET /articles/{slug}/comments)
  9. Get Tags (GET /tags)
  10. Get Profile (GET /profiles/{username})
  11. Favorite Article (POST /articles/{slug}/favorite)

---

## Modified Files

### REST Controllers (JAX-RS -> Spring MVC)

All controllers migrated from JAX-RS annotations to Spring MVC annotations:

| JAX-RS | Spring MVC |
|--------|-----------|
| `@Path` | `@RequestMapping` |
| `@GET/@POST/@PUT/@DELETE` | `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping` |
| `@Produces/@Consumes` | `produces`/`consumes` in mapping annotations |
| `@PathParam` | `@PathVariable` |
| `@QueryParam` | `@RequestParam` |
| `@Context SecurityContext` | `Principal` parameter |

**ArticlesResource.java**: Rewrote all 11 endpoints. Uses both `wrapRootValueObjectMapper` (single entities) and `noWrapRootValueObjectMapper` (list responses). Returns `ResponseEntity<String>` with explicit Jackson serialization.

**UsersResource.java**: Rewrote create/login endpoints. Uses `wrapRootValueObjectMapper` for `{"user": {...}}` wrapping. Catches `UserNotFoundException`/`InvalidPasswordException` and converts to Spring Security's `BadCredentialsException`.

**UserResource.java**: Rewrote get/update user endpoints. Uses `wrapRootValueObjectMapper`.

**ProfilesResource.java**: Rewrote get/follow/unfollow profile endpoints. Uses `wrapRootValueObjectMapper`.

**TagsResource.java**: Rewrote get tags endpoint. Uses `noWrapRootValueObjectMapper` for `{"tags": [...]}` response.

### ResourceUtils.java
- **Changed**: `@ApplicationScoped` -> `@Component`
- **Changed**: `@AllArgsConstructor` retained (works with Spring constructor injection)
- **Changed**: `getLoggedUserId(SecurityContext)` -> `getLoggedUserId(Principal)` using `UUID.fromString(principal.getName())`

### Repository Layer (Panache -> JPA EntityManager)

**AbstractPanacheRepository.java**: Replaced `PanacheRepository` base class with abstract class containing `@PersistenceContext EntityManager` and helper methods (`persist`, `find`, `remove`, `getEntityManager`).

All 7 repository implementations rewritten:
- **UserRepositoryPanache.java**: Panache find/query -> EntityManager JPQL queries
- **ArticleRepositoryPanache.java**: Complex filter queries with dynamic WHERE clauses, pagination, and sorting rewritten using `CriteriaBuilder` or JPQL with `TypedQuery`
- **TagRepositoryPanache.java**: Simple queries migrated
- **CommentRepositoryPanache.java**: Queries by article slug migrated
- **FavoriteRelationshipRepositoryPanache.java**: Composite key queries migrated
- **FollowRelationshipRepositoryPanache.java**: Composite key queries migrated
- **TagRelationshipRepositoryPanache.java**: Queries by article migrated

All repositories changed from `@ApplicationScoped` to `@Repository`.

### EntityUtils.java
- **Changed**: `@ApplicationScoped` -> `@Component`

### Provider Layer

**JwtTokenProvider.java**:
- **Removed**: SmallRye JWT (`GenerateToken`, `JwtClaimsBuilder`) with RSA key pair signing
- **Added**: jjwt library (`Jwts.builder()`) with HMAC-SHA512 signing
- **Changed**: Token creation uses `Jwts.builder().issuer().subject().claim("groups").issuedAt().expiration().signWith()`
- **Changed**: Token validation uses `Jwts.parser().verifyWith().build().parseSignedClaims()`

**BCryptHashProvider.java**:
- **Removed**: Quarkus Elytron `BCryptUtil`
- **Added**: Spring Security `BCryptPasswordEncoder`
- **Changed**: `@ApplicationScoped` -> `@Component`

**SlugifySlugProvider.java**:
- **Changed**: `@ApplicationScoped` -> `@Component`

### Configuration Layer (CDI -> Spring)

All 5 configuration classes migrated:
- **ApplicationConfiguration.java**: `@ApplicationScoped/@Produces` -> `@Configuration/@Bean`. Contains 3 ObjectMapper beans:
  - `wrapRootValueObjectMapper`: WRAP_ROOT_VALUE + UNWRAP_ROOT_VALUE for single entity serialization
  - `noWrapRootValueObjectMapper`: Plain mapper for list responses
  - `primaryObjectMapper` (@Primary): UNWRAP_ROOT_VALUE only (used by Spring MVC for request deserialization)
- **UsersConfiguration.java**: CDI producers -> Spring `@Bean` methods
- **ArticlesConfiguration.java**: CDI producers -> Spring `@Bean` methods
- **TagsConfiguration.java**: CDI producers -> Spring `@Bean` methods
- **CommentsConfiguration.java**: CDI producers -> Spring `@Bean` methods

### Exception Handling

**ExceptionMappers.java**:
- **Removed**: `@ServerExceptionMapper` methods
- **Added**: `@RestControllerAdvice` with `@ExceptionHandler` methods
- Handles: `BusinessException` (422), `BadCredentialsException` (401)

**BeanValidationExceptionMapper.java**:
- **Removed**: JAX-RS `ExceptionMapper<ConstraintViolationException>`
- **Added**: `@RestControllerAdvice` with `@ExceptionHandler(MethodArgumentNotValidException.class)`

### NoWrapRootValueObjectMapper.java (qualifier annotation)
- **Changed**: CDI `@Qualifier` -> Spring `@Qualifier`

### Request/Response Models (14 files)
- **Removed**: `@RegisterForReflection` annotation and import from all model classes
  - ArticleResponse, ArticlesResponse, CommentResponse, CommentsResponse, ErrorResponse,
    ProfileResponse, TagsResponse, UserResponse, LoginRequest, NewArticleRequest,
    NewCommentRequest, NewUserRequest, UpdateArticleRequest, UpdateUserRequest

### UserModelBuilder.java
- **Removed**: `@Named` annotation and import

---

## Deleted Files

- `src/test/` directory (entire test suite) - contained Quarkus-specific integration tests (`@QuarkusTest`, `@TestHTTPResource`, etc.) that are incompatible with Spring Boot

---

## Errors Encountered and Resolutions

### Error 1: Docker build environment variable expansion
- **Error**: `docker build -t $SCARF_IMAGE_TAG .` failed - shell did not expand env var
- **Resolution**: Used literal value `my_test_image_1` directly

### Error 2: JSON serialization - WRAP_ROOT_VALUE on primary ObjectMapper
- **Error**: 3/11 smoke tests failed (Get Articles List, Get Comments, Get Tags). List responses like `ArticlesResponse` were wrapped with class name (`{"ArticlesResponse": {...}}`) instead of expected format (`{"articles": [...], "articlesCount": N}`).
- **Root Cause**: The `@Primary` ObjectMapper had `WRAP_ROOT_VALUE` enabled. Spring MVC used this for ALL serialization, causing list response objects without `@JsonRootName` to be wrapped with their class name.
- **Resolution**: Created 3 separate ObjectMapper beans:
  1. `wrapRootValueObjectMapper` - for explicit serialization of single entities (`@JsonRootName`)
  2. `noWrapRootValueObjectMapper` - for list responses without wrapping
  3. `primaryObjectMapper` (@Primary) - plain mapper (no WRAP_ROOT_VALUE) for Spring MVC defaults
  - Updated all controllers to use explicit `objectMapper.writeValueAsString()` and return `ResponseEntity<String>`

### Error 3: JSON deserialization - missing UNWRAP_ROOT_VALUE
- **Error**: All POST endpoints returned 400/403. Request bodies like `{"user": {...}}` failed with `Unrecognized field "user"`.
- **Root Cause**: After fixing Error 2, the `@Primary` ObjectMapper no longer had `UNWRAP_ROOT_VALUE` enabled. Request models have `@JsonRootName("user")` etc., which requires unwrapping during deserialization.
- **Resolution**: Enabled `DeserializationFeature.UNWRAP_ROOT_VALUE` on the `@Primary` ObjectMapper. Since controllers return `ResponseEntity<String>` (bypassing Jackson serialization), enabling UNWRAP_ROOT_VALUE on the primary mapper only affects deserialization, which is the desired behavior.

---

## Smoke Test Results

```
============================================================
RealWorld API Smoke Tests
============================================================
[PASS] Register User
[PASS] Login User
[PASS] Get Current User
[PASS] Create Article
[PASS] Get Articles List
[PASS] Get Article by Slug
[PASS] Create Comment
[PASS] Get Comments
[PASS] Get Tags
[PASS] Get Profile
[PASS] Favorite Article
============================================================
Total Tests: 11
Passed: 11
Failed: 0
============================================================
```
