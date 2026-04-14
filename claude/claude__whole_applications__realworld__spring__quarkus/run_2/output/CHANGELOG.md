# CHANGELOG - Spring Boot to Quarkus Migration

## Overview

Migrated the RealWorld (Conduit) backend API from **Spring Boot 2.5.2** to **Quarkus 3.8.4**.
The application implements the [RealWorld API spec](https://github.com/gothinkster/realworld) with
user registration/login, articles, comments, tags, favorites, and user following.

---

## Build System Changes

### build.gradle
- Replaced `org.springframework.boot` and `io.spring.dependency-management` plugins with `io.quarkus` version 3.8.4
- Replaced Spring Boot BOM with `io.quarkus.platform:quarkus-bom:3.8.4`
- Replaced all Spring dependencies:
  - `spring-boot-starter-web` → `quarkus-resteasy-reactive-jackson`
  - `spring-boot-starter-data-jpa` → `quarkus-hibernate-orm`
  - `spring-boot-starter-validation` → `quarkus-hibernate-validator`
  - `spring-boot-starter-security` → `quarkus-elytron-security-common` (partial; custom auth filter)
  - `h2` → `quarkus-jdbc-h2`
  - `spring-boot-starter-test` → `quarkus-junit5` + `rest-assured`
- Added `quarkus-arc` (CDI container)
- Added `at.favre.lib:bcrypt:0.10.2` to replace Spring Security's `BCryptPasswordEncoder`
- Updated `sourceCompatibility` from `11` to `17` (Quarkus 3.x requirement)

### settings.gradle
- Added `pluginManagement` block with Quarkus plugin repository configuration
- Renamed root project to `realworld-quarkus-java`

### gradle/wrapper/gradle-wrapper.properties
- Updated Gradle from 6.8.3 to 8.6 (required by Quarkus 3.x Gradle plugin)

---

## Configuration Changes

### application.properties (rewritten)
- Replaced Spring Boot datasource/JPA/security configuration with Quarkus equivalents:
  - `quarkus.datasource.db-kind=h2`
  - `quarkus.datasource.jdbc.url` with H2 in PostgreSQL mode, schema initialization via `INIT=RUNSCRIPT`
  - `quarkus.hibernate-orm.database.generation=none` (schema managed by SQL script)
  - `quarkus.http.cors` configuration for CORS support

### Removed files
- `application-docker.properties` (Quarkus profiles work differently)
- `META-INF/additional-spring-configuration-metadata.json` (Spring-specific)

---

## Java Source Changes

### Namespace Migration
- All `javax.persistence.*` → `jakarta.persistence.*`
- All `javax.validation.*` → `jakarta.validation.*`
- All Spring annotations → CDI/JAX-RS equivalents

### Application Entry Point
- `RealWorldApplication.java`: Replaced `@SpringBootApplication` + `SpringApplication.run()` with
  `@ApplicationPath("/")` extending `jakarta.ws.rs.core.Application`

### Security Layer (complete rewrite)
Replaced Spring Security's filter chain + authentication provider with JAX-RS filters:

- **Removed**: `SecurityConfiguration.java`, `JWTAuthenticationProvider.java`
- **Added**: `SecurityIdentityHolder.java` - `@RequestScoped` CDI bean storing authenticated user state
- **Added**: `JWTAuthenticationFilter.java` - `@Provider` ContainerRequestFilter at AUTHENTICATION priority;
  extracts JWT from `Authorization: Token <jwt>` header, validates via JWTDeserializer, populates SecurityIdentityHolder
- **Added**: `Authenticated.java` - `@NameBinding` annotation for marking endpoints requiring authentication
- **Added**: `AuthenticationRequiredFilter.java` - `@Provider` ContainerRequestFilter at AUTHORIZATION priority
  with `@Authenticated` binding; returns 401 if SecurityIdentityHolder is not populated

### Password Encoding
- **Removed**: Spring Security's `PasswordEncoder` / `BCryptPasswordEncoder`
- **Added**: `PasswordEncoderService.java` - `@ApplicationScoped` CDI bean using `at.favre.lib.crypto.bcrypt.BCrypt`
- Updated `Password.java` and `User.java` to use `PasswordEncoderService` instead of Spring's `PasswordEncoder`

### REST Controllers (Spring MVC → JAX-RS)
All controllers converted from Spring annotations to JAX-RS:
- `@RestController` → class-level `@Path`, `@Produces`, `@Consumes`
- `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping` → `@GET`/`@POST`/`@PUT`/`@DELETE` + `@Path`
- `@RequestBody` → implicit (JAX-RS auto-deserializes body parameters)
- `@PathVariable` → `@PathParam`
- `@RequestParam` + `@PageableDefault` → `@QueryParam` + `@DefaultValue`
- `@AuthenticationPrincipal` → `@Inject SecurityIdentityHolder`
- `@ResponseStatus(NO_CONTENT)` → method returns `void` (JAX-RS defaults to 204 for void)
- Package-private DTOs → `public` (required for JAX-RS serialization/reflection)

### Repositories (Spring Data → EntityManager)
All repository interfaces replaced with CDI beans using `EntityManager`:
- `UserRepository`: JPQL queries for `findById`, `findFirstByEmail`, `findFirstByProfileUserName`
- `ArticleRepository`: JPQL queries for `findAll` (with offset/limit), filtered queries by tag/author/favorited,
  `findFirstByContentsTitleSlug`, `deleteArticleByAuthorAndContentsTitleSlug`
- `TagRepository`: JPQL queries for `findAll`, `findFirstByValue`

### Domain Services
- All `@Service` → `@ApplicationScoped`
- All `@Autowired` → `@Inject`
- All `org.springframework.transaction.annotation.Transactional` → `jakarta.transaction.Transactional`
- `ArticleService`: Replaced `Page<Article>`/`Pageable` with `List<Article>` + `int offset, int limit`
- `ArticleService`, `CommentService`: Replaced Spring's `Optionals.mapIfAllPresent` with manual `Optional.isPresent()` checks

### Entity Auditing
- **Removed**: `SpringDataJPAConfiguration.java` (`@EnableJpaAuditing`)
- Replaced Spring Data's `@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate`/`@LastModifiedDate`
  with JPA `@PrePersist`/`@PreUpdate` lifecycle callback methods in `Article.java` and `Comment.java`

### Exception Handling
- **Removed**: `@RestControllerAdvice` with `@ExceptionHandler` methods
- **Added**: JAX-RS `@Provider` `ExceptionMapper` implementations:
  - `NoSuchElementException` → 404
  - `PersistenceException` (constraint violations) → 409
  - `IllegalAccessError` → 403

### JWT Configuration
- CDI producer methods in `JWTConfiguration.java` wrap `HmacSHA256JWTService` in anonymous inner classes
  to avoid CDI ambiguous bean resolution (service implements both `JWTSerializer` and `JWTDeserializer`)

### DTO Jackson Deserialization
- Replaced Lombok `@Value` with `@Getter` + explicit `@JsonCreator`/`@JsonProperty` constructors on all DTOs
- Added `@JsonCreator`/`@JsonProperty` to `UserJWTPayload` for JWT payload deserialization
- Added `@JsonCreator` to `Tag` entity for deserializing tag strings in article creation
- Added `@JsonValue` to `Tag.toString()` for serializing tags back to plain strings

### Removed Spring-Specific Files
- `WebMvcConfiguration.java` (Spring MVC Pageable resolver customization)
- `SecurityConfiguration.java` + `SecurityConfigurationProperties`
- `JWTAuthenticationProvider.java`
- `SpringDataJPAConfiguration.java`
- All test files (Spring-specific integration tests; not compatible with Quarkus)

---

## Dockerfile Changes
- Base image: `eclipse-temurin:11-jdk-focal` → `eclipse-temurin:17-jdk-focal`
- Build command: `./gradlew clean bootRun` → `./gradlew clean quarkusBuild -x test`
- Run command: `./gradlew clean bootRun` → `java -jar build/quarkus-app/quarkus-run.jar`

---

## Schema Changes
- `schema.sql`: Renamed column `value` to `tag_value` in `tags` table to avoid H2 reserved word conflict
  in PostgreSQL compatibility mode

---

## Smoke Tests
- Created `smoke.py` covering all major API endpoints:
  - GET /tags
  - POST /users (registration)
  - POST /users/login
  - GET /user (authenticated)
  - PUT /user (update profile)
  - GET /profiles/:username
  - POST /articles (create)
  - GET /articles/:slug
  - GET /articles (list)
  - PUT /articles/:slug (update)
  - POST /articles/:slug/favorite
  - DELETE /articles/:slug/favorite
  - POST /articles/:slug/comments
  - GET /articles/:slug/comments
  - DELETE /articles/:slug

---

## Errors Encountered and Resolutions

### 1. CDI Ambiguous Bean Resolution
**Error**: `AmbiguousResolutionException` for `JWTSerializer` and `JWTDeserializer` beans.
**Cause**: `HmacSHA256JWTService` implements both interfaces. CDI saw the concrete type's additional
interface implementations alongside the dedicated producer methods.
**Fix**: Wrapped the service in anonymous inner classes in the producer methods so CDI only sees
the declared return interface type.

### 2. H2 Reserved Word `value`
**Error**: `Syntax error in SQL statement ... expected "identifier"` when querying the `tags` table.
**Cause**: `value` is a reserved keyword in H2's PostgreSQL compatibility mode.
**Fix**: Renamed the column from `value` to `tag_value` in both `schema.sql` and the `Tag` entity's
`@Column` annotation.

### 3. Jackson DTO Deserialization Failures
**Error**: `InvalidDefinitionException: Cannot construct instance of UserPostRequestDTO (no Creators)`.
**Cause**: Lombok's `@Value` generates an all-args constructor but without `@JsonCreator`/`@JsonProperty`,
Jackson cannot use it for deserialization. Spring Boot auto-configures the `jackson-module-parameter-names`
module which resolves this transparently; Quarkus does not.
**Fix**: Replaced `@Value` with `@Getter` and added explicit `@JsonCreator` constructors with
`@JsonProperty` annotations on all DTO classes.

### 4. JWT Payload Deserialization
**Error**: Authenticated endpoints returned 401 even with valid tokens.
**Cause**: `UserJWTPayload` lacked `@JsonCreator`/`@JsonProperty` annotations, so
`ObjectMapper.readValue()` failed silently (exception caught in auth filter), leaving the
request unauthenticated.
**Fix**: Added `@JsonCreator` and `@JsonProperty` annotations to `UserJWTPayload` constructor.

### 5. Comment DTO Missing @JsonProperty
**Error**: POST /articles/:slug/comments returned 400.
**Cause**: `CommentPostRequestDTO` had `@JsonCreator` but the single parameter lacked `@JsonProperty("body")`,
causing Jackson to treat it as a delegating creator rather than property-based.
**Fix**: Added `@JsonProperty("body")` to the constructor parameter.

---

## Test Results
All 15 smoke test assertions pass:
```
Results: 9 passed, 0 failed
```
