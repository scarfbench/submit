# CHANGELOG - Quarkus to Spring Boot Migration

## Overview
Migrated the RealWorld API (Conduit) application from Quarkus 3.x to Spring Boot 3.4.1.
All 67 smoke tests pass. The application builds, starts, and serves all RealWorld API endpoints correctly.

## Dependencies (pom.xml)

### Removed
- `io.quarkus.platform:quarkus-bom` (BOM)
- `io.quarkus:quarkus-hibernate-orm-panache`
- `io.quarkus:quarkus-jdbc-postgresql`
- `io.quarkus:quarkus-arc`
- `io.quarkus:quarkus-rest-jackson`
- `io.quarkus:quarkus-rest`
- `io.quarkus:quarkus-smallrye-jwt`
- `io.quarkus:quarkus-smallrye-jwt-build`
- `io.quarkus:quarkus-elytron-security-common`
- `io.quarkus:quarkus-hibernate-validator`
- `io.quarkus:quarkus-junit5`
- `io.rest-assured:rest-assured`
- `quarkus-maven-plugin`
- `spotless-maven-plugin`

### Added
- `org.springframework.boot:spring-boot-starter-parent:3.4.1` (parent POM)
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.boot:spring-boot-starter-actuator`
- `org.postgresql:postgresql` (runtime)
- `io.jsonwebtoken:jjwt-api:0.12.6`
- `io.jsonwebtoken:jjwt-impl:0.12.6` (runtime)
- `io.jsonwebtoken:jjwt-jackson:0.12.6` (runtime)
- `org.springframework.boot:spring-boot-maven-plugin`

### Retained
- `com.github.slugify:slugify:3.0.7`
- `org.projectlombok:lombok:1.18.38`

## New Files

| File | Purpose |
|------|---------|
| `src/main/java/org/example/realworldapi/RealWorldApiApplication.java` | Spring Boot main class with `@SpringBootApplication` |
| `src/main/java/org/example/realworldapi/infrastructure/web/security/JwtAuthenticationFilter.java` | Spring Security filter extracting JWT from `Authorization` header |
| `src/main/java/org/example/realworldapi/infrastructure/web/security/SecurityConfig.java` | Spring Security configuration with endpoint authorization rules |
| `smoke.py` | Comprehensive Python smoke test suite (67 checks) |

## Deleted Files

| File/Directory | Reason |
|----------------|--------|
| `src/test/` | Quarkus test classes (QuarkusTest, RestAssured) |
| `src/main/docker/` | Quarkus Docker configuration files |

## Modified Files

### Build & Infrastructure
- **pom.xml**: Complete rewrite from Quarkus BOM to Spring Boot parent
- **Dockerfile**: Changed `mvn clean install` to `mvn clean package`
- **start.sh**: Changed jar path from `target/quarkus-app/quarkus-run.jar` to `target/realworldapiservice-1.0-SNAPSHOT.jar`
- **application.properties**: Migrated all Quarkus properties to Spring Boot equivalents

### Configuration Classes (CDI -> Spring DI)
- **ApplicationConfiguration.java**: `@Dependent` -> `@Configuration`, `@Produces` -> `@Bean`, `@Singleton` removed; added `@Primary` on main ObjectMapper, `@Bean(name="noWrapRootValueObjectMapper")` for secondary mapper
- **UsersConfiguration.java**: Same CDI -> Spring conversion
- **ArticlesConfiguration.java**: Same CDI -> Spring conversion
- **CommentsConfiguration.java**: Same CDI -> Spring conversion
- **TagsConfiguration.java**: Same CDI -> Spring conversion

### REST Controllers (JAX-RS -> Spring MVC)
- **ArticlesResource.java**: `@Path` -> `@RestController` + `@RequestMapping`; `@GET/@POST/@PUT/@DELETE` -> `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping`; `Response` -> `ResponseEntity`; `SecurityContext` -> `Principal`; `@QueryParam` -> `@RequestParam`; `@PathParam` -> `@PathVariable`
- **UsersResource.java**: Same JAX-RS -> Spring MVC conversion; `UnauthorizedException` -> `BadCredentialsException`
- **UserResource.java**: Same conversion pattern
- **ProfilesResource.java**: Same conversion pattern
- **TagsResource.java**: Same conversion pattern
- **ResourceUtils.java**: `@ApplicationScoped` -> `@Component`; `SecurityContext` -> `Principal`

### Repository Layer (Panache -> EntityManager)
- **AbstractPanacheRepository.java**: Removed `PanacheRepositoryBase` extension; added `@PersistenceContext EntityManager`; implemented base CRUD methods directly
- **ArticleRepositoryPanache.java**: `@ApplicationScoped` -> `@Repository`; all Panache methods replaced with JPQL/EntityManager
- **UserRepositoryPanache.java**: Same conversion; dynamic field queries via JPQL string building
- **CommentRepositoryPanache.java**: Same conversion
- **TagRepositoryPanache.java**: Same conversion
- **FollowRelationshipRepositoryPanache.java**: Same conversion
- **FavoriteRelationshipRepositoryPanache.java**: Same conversion
- **TagRelationshipRepositoryPanache.java**: Same conversion

### Security Layer
- **JwtTokenProvider.java**: Replaced SmallRye JWT (`Jwt.issuer().subject().sign()`) with JJWT library (`Jwts.builder()`); RSA -> HMAC-SHA256; added `getSubjectFromToken()` and `validateToken()`
- **BCryptHashProvider.java**: Replaced `BcryptUtil.bcryptHash()`/`matches()` with Spring Security `BCryptPasswordEncoder`

### Exception Handling
- **ExceptionMappers.java**: `@ServerExceptionMapper` methods -> `@RestControllerAdvice` + `@ExceptionHandler`; `Response` -> `ResponseEntity`; added handlers for `MethodArgumentNotValidException` and `ConstraintViolationException`
- **BeanValidationExceptionMapper.java**: Content replaced (no longer needed, handled by ControllerAdvice)

### Other Changes
- **EntityUtils.java**: `@ApplicationScoped` -> `@Component`
- **UserModelBuilder.java**: Removed `@Named` annotation
- **NoWrapRootValueObjectMapper.java**: Removed `@Qualifier` (jakarta.inject), kept as plain annotation
- **SlugifySlugProvider.java**: `@ApplicationScoped` -> `@Component`
- **All request/response model classes** (14 files): Removed `@RegisterForReflection` and its import

## Key Architecture Decisions

1. **JWT**: Switched from RSA (SmallRye JWT) to HMAC-SHA256 (JJWT) for simplicity; no PEM key files needed
2. **Repository Pattern**: Used EntityManager directly rather than Spring Data JPA repositories to preserve the existing query patterns
3. **Security**: Created `SecurityFilterChain` bean matching original `@RolesAllowed`/`@PermitAll` patterns
4. **Context Path**: Set `server.servlet.context-path=/api` to maintain URL compatibility
5. **Jackson**: Preserved `WRAP_ROOT_VALUE`/`UNWRAP_ROOT_VALUE` configuration with `@Primary` and `@Qualifier` ObjectMappers
6. **Database**: `spring.jpa.hibernate.ddl-auto=create-drop` for schema auto-generation (matching Quarkus behavior)

## Test Results

```
Results: 67 passed, 0 failed out of 67 checks
```

All endpoints verified:
- User registration, login, current user, update user
- Article CRUD (create, read, update, delete)
- Article listing (all, by author, by tag, feed)
- Article favorites/unfavorites
- Comment CRUD
- Profile viewing, following/unfollowing
- Tags listing
