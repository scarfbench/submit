# CHANGELOG: Spring Boot to Quarkus Migration

## Overview

Migrated the RealWorld (Conduit) REST API application from **Spring Boot 2.5.2** (Gradle, Java 11) to **Quarkus 3.2.12.Final** (Maven, Java 17).

---

## Build System

### Changed: Gradle to Maven
- **Deleted**: `build.gradle`, `settings.gradle`, `test.gradle`
- **Created**: `pom.xml`
  - Quarkus BOM: `io.quarkus.platform:quarkus-bom:3.2.12.Final`
  - Dependencies: `quarkus-resteasy-jackson`, `quarkus-hibernate-orm`, `quarkus-jdbc-h2`, `quarkus-hibernate-validator`, `quarkus-arc`, `jbcrypt:0.4`, `lombok:1.18.36`
  - Java version: 17 (required by Quarkus 3.x / Jakarta EE 10)
  - Test deps: `quarkus-junit5`, `rest-assured`

---

## Configuration

### Changed: `application.properties`
- Replaced Spring Boot config with Quarkus equivalents:
  - `spring.datasource.*` -> `quarkus.datasource.*`
  - `spring.jpa.*` -> `quarkus.hibernate-orm.*`
  - Added CORS configuration (`quarkus.http.cors.*`)
  - Schema generation: `quarkus.hibernate-orm.database.generation=drop-and-create` (Hibernate auto-generates from entities)

### Deleted
- `application-docker.properties` (Quarkus doesn't use Spring profiles the same way)
- `src/main/resources/META-INF/additional-spring-configuration-metadata.json`

---

## Java Source Changes

### Namespace Migration
- All `javax.persistence.*` imports -> `jakarta.persistence.*`
- All `javax.validation.*` imports -> `jakarta.validation.*`
- All `javax.inject.*` imports -> `jakarta.inject.*`

### Application Entry Point
- **`RealWorldApplication.java`**: `@SpringBootApplication` + `SpringApplication.run()` -> `@QuarkusMain` + `Quarkus.run(args)`

### REST Controllers (Spring MVC -> JAX-RS)
All controllers rewritten from Spring MVC annotations to JAX-RS:
- `@RestController` -> `@Path` + `@Produces` + `@Consumes`
- `@GetMapping` -> `@GET` + `@Path`
- `@PostMapping` -> `@POST` + `@Path`
- `@PutMapping` -> `@PUT` + `@Path`
- `@DeleteMapping` -> `@DELETE` + `@Path`
- `@RequestBody` -> parameter without annotation (JAX-RS auto-deserializes)
- `@PathVariable` -> `@PathParam`
- `@RequestParam` -> `@QueryParam` + `@DefaultValue`
- `@AuthenticationPrincipal JWTPayload` -> `@Inject AuthenticatedUserContext`

**Files changed**:
- `UserRestController.java`
- `ProfileRestController.java`
- `ArticleRestController.java`
- `CommentRestController.java`
- `TagRestController.java`

### Authentication & Security
- **Deleted**: `SecurityConfiguration.java`, `JWTAuthenticationProvider.java`
- **Created**: `AuthenticatedUserContext.java` - `@RequestScoped` CDI bean holding JWT payload and token
- **Rewritten**: `JWTAuthenticationFilter.java`
  - Was: Spring `OncePerRequestFilter` using Spring Security context
  - Now: JAX-RS `ContainerRequestFilter` with `@Provider` annotation that populates `AuthenticatedUserContext` CDI bean

### Dependency Injection
- Spring `@Service` -> CDI `@ApplicationScoped`
- Spring `@Configuration` + `@Bean` -> CDI `@ApplicationScoped` + `@Produces`
- Spring constructor injection -> CDI `@Inject` field injection
- Spring `@Transactional` -> `jakarta.transaction.Transactional`

**Files changed**:
- `UserService.java`
- `ProfileService.java`
- `ArticleService.java`
- `CommentService.java`
- `TagService.java`
- `JWTConfiguration.java`

### Data Access (Spring Data JPA -> CDI + EntityManager)
- **Deleted**: `SpringDataJPAConfiguration.java`
- Spring Data `Repository` interfaces -> CDI `@ApplicationScoped` classes with `@Inject EntityManager` and explicit JPQL queries

**Files changed**:
- `UserRepository.java` - Rewrote all derived query methods as JPQL
- `ArticleRepository.java` - Rewrote with offset/limit parameters instead of `Pageable`
- `TagRepository.java` - Rewrote `findAll()` and `findFirstByValue()` as JPQL

### Pagination
- Spring `Page<Article>` / `Pageable` -> `List<Article>` with `int offset, int limit` parameters

### Password Encoding
- Spring `PasswordEncoder` (BCryptPasswordEncoder) -> jBCrypt library directly (`org.mindrot.jbcrypt.BCrypt`)
- `Password.of(rawPassword, passwordEncoder)` -> `Password.of(rawPassword)`
- `password.matchesPassword(rawPassword, passwordEncoder)` -> `password.matchesPassword(rawPassword)`

### Auditing
- Spring `@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate` / `@LastModifiedDate` -> JPA `@PrePersist` / `@PreUpdate` lifecycle callbacks

**Files changed**: `Article.java`, `Comment.java`

### Entity Fixes
- `Tag.java`: `@Column(name = "value")` -> `@Column(name = "\`value\`")` (H2 reserved keyword quoting)
- `Article.java`: `comments` collection changed to `fetch = EAGER` to prevent `LazyInitializationException`
- `User.java`: `followingUsers` and `articleFavorited` collections changed to `fetch = EAGER`
- `ArticleContents.java`: Increased `description` column length to 10000, `body` to 100000
- `Comment.java`: Increased `body` column length to 10000

### Exception Handling
- Spring `@RestControllerAdvice` + `@ExceptionHandler` -> JAX-RS `@Provider` + `ExceptionMapper<T>`

### Deleted (Spring-only)
- `WebMvcConfiguration.java`
- All `src/test/` Spring test files

### Not Modified (pure Java, no Spring dependencies)
- `UserFindService.java`, `ArticleFindService.java`
- `UserSignUpRequest.java`, `UserUpdateRequest.java`, `ArticleUpdateRequest.java`
- `JWTPayload.java`, `JWTSerializer.java`, `JWTDeserializer.java`
- `HmacSHA256JWTService.java`, `Base64URL.java`, `HmacSHA256.java`, `UserJWTPayload.java`
- All Lombok DTOs (no Spring imports)

---

## Docker

### Changed: `Dockerfile`
- Base image: `eclipse-temurin:11-jdk-focal` -> `eclipse-temurin:17-jdk-focal`
- Build tool: Gradle (`./gradlew clean bootRun`) -> Maven 3.9.9 (`mvn package -DskipTests`)
- Run command: Spring Boot fat jar -> Quarkus runner jar (`java -jar target/quarkus-app/quarkus-run.jar`)
- Maven installed from `archive.apache.org` (system Maven too old for Quarkus plugin)

---

## Testing

### Created: `smoke.py`
Comprehensive Python smoke test covering 12 tests:
1. Health check (GET /tags)
2. Get tags
3. Register user (POST /users)
4. Login user (POST /users/login)
5. Get current user (GET /user)
6. Update user (PUT /user)
7. Get profile (GET /profiles/:username)
8. Create article (POST /articles)
9. Get articles (GET /articles)
10. Get article by slug (GET /articles/:slug)
11. Add comment (POST /articles/:slug/comments)
12. Get comments (GET /articles/:slug/comments)

**Result: All 12 tests pass.**

---

## Errors Encountered and Resolutions

### 1. Quarkus 3.x requires Java 17
- **Error**: Compilation failed with `maven.compiler.release=11`
- **Fix**: Changed to Java 17 in `pom.xml` and Dockerfile base image

### 2. System Maven too old for Quarkus plugin
- **Error**: `No implementation for io.quarkus.maven.QuarkusBootstrapProvider was bound`
- **Fix**: Installed Maven 3.9.9 from `archive.apache.org` in Dockerfile

### 3. Maven download URL 404
- **Error**: `https://dlcdn.apache.org/maven/maven-3/3.9.6/` returned 404
- **Fix**: Used `https://archive.apache.org/dist/maven/maven-3/3.9.9/`

### 4. H2 reserved keyword `value`
- **Error**: `Syntax error in SQL statement... expected "identifier"` when creating `tags` table
- **Fix**: Used backtick quoting in JPA annotation: `@Column(name = "\`value\`")`

### 5. ContainerRequestContext not injectable in RESTEasy Classic
- **Error**: `RESTEASY003880: Unable to find contextual data of type: jakarta.ws.rs.container.ContainerRequestContext`
- **Fix**: Created `@RequestScoped` `AuthenticatedUserContext` CDI bean, populated by JWT filter, injected into resource methods

### 6. LazyInitializationException on comments collection
- **Error**: `failed to lazily initialize a collection of role: Article.comments: could not initialize proxy - no Session`
- **Fix**: Changed `@OneToMany` and `@ManyToMany` collections to `fetch = EAGER` on `Article` and `User` entities
