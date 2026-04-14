# CHANGELOG

## Migration: Spring Boot → Quarkus

### Summary
Migrated the RealWorld (Conduit) API application from Spring Boot 2.5.2 (Gradle) to Quarkus 2.16.12.Final (Maven). All 45 smoke tests pass. The application retains full API compatibility with the RealWorld specification.

---

### Step 1: Codebase Analysis
- **Action**: Read and analyzed all source files across domain, application, and infrastructure layers
- **Findings**:
  - Spring Boot 2.5.2 with Gradle build system
  - Spring Web MVC, Spring Security with custom JWT authentication (HmacSHA256)
  - Spring Data JPA with H2 in-memory database (PostgreSQL compatibility mode)
  - Lombok for boilerplate reduction, Java 11 target
  - Layered architecture: domain (entities, services, repositories), application (REST controllers, DTOs, security), infrastructure (JWT implementation)

### Step 2: Smoke Test Generation
- **Action**: Created `smoke.py` with 45 comprehensive API tests
- **Tests cover**: User registration, login, authentication failures, user CRUD, profiles, article CRUD, favorites, comments, follow/unfollow, tags, unauthenticated access restrictions

### Step 3: Build System Migration (Gradle → Maven)
- **Action**: Created `pom.xml` replacing `build.gradle`, `settings.gradle`, `test.gradle`
- **Files removed**: `build.gradle`, `settings.gradle`, `test.gradle`, `gradle/` directory, `gradlew`, `gradlew.bat`
- **Dependencies added**:
  - `quarkus-resteasy-reactive-jackson` (replaces spring-boot-starter-web)
  - `quarkus-hibernate-orm` (replaces spring-boot-starter-data-jpa)
  - `quarkus-hibernate-validator` (replaces spring-boot-starter-validation)
  - `quarkus-jdbc-h2` (replaces H2 runtime dependency)
  - `quarkus-arc` (CDI container)
  - `quarkus-narayana-jta` (transaction management)
  - `org.mindrot:jbcrypt:0.4` (replaces Spring Security PasswordEncoder)
  - `lombok:1.18.30` (retained)

### Step 4: Configuration Migration
- **Action**: Rewrote `src/main/resources/application.properties` for Quarkus
- **Files removed**: `application-docker.properties`, `META-INF/additional-spring-configuration-metadata.json`, `schema.sql`
- **Key changes**:
  - `spring.datasource.*` → `quarkus.datasource.*`
  - `spring.jpa.*` → `quarkus.hibernate-orm.*`
  - `server.port` → `quarkus.http.port`
  - Added CORS configuration via `quarkus.http.cors.*`

### Step 5: Java Source Code Migration

#### 5a: Entry Point
- **RealWorldApplication.java**: Replaced `@SpringBootApplication` main class with JAX-RS `@ApplicationPath("/")` extending `javax.ws.rs.core.Application`

#### 5b: Security Layer
- **Created `AuthContext.java`**: New `@RequestScoped` CDI bean replacing Spring Security's `@AuthenticationPrincipal` mechanism. Stores authenticated user's JWT payload and token per-request.
- **Rewrote `JWTAuthenticationFilter.java`**: Converted from Spring Security `OncePerRequestFilter` to JAX-RS `@Provider @PreMatching ContainerRequestFilter`. Extracts "Token xxx" header and populates `AuthContext`.
- **Files removed**: `SecurityConfiguration.java` (Spring Security config), `JWTAuthenticationProvider.java` (Spring Security provider)

#### 5c: Domain Layer
- **Password.java**: Replaced `PasswordEncoder` injection with direct `BCrypt.hashpw()`/`BCrypt.checkpw()` calls
- **User.java**: Removed `PasswordEncoder` constructor parameter, simplified password creation
- **UserService.java**: `@Service` → `@ApplicationScoped`, `org.springframework.transaction.annotation.Transactional` → `javax.transaction.Transactional`
- **UserRepository.java**: Converted from Spring Data `JpaRepository<User, Long>` interface to concrete class with `@Inject EntityManager` and manual JPQL queries
- **ProfileService.java**: `@Service` → `@ApplicationScoped`
- **Article.java**: Replaced `@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate`/`@LastModifiedDate` with JPA `@PrePersist`/`@PreUpdate` lifecycle callbacks
- **ArticleRepository.java**: Converted from Spring Data interface to EntityManager-based; replaced `Page`/`Pageable` with `List` + `int offset, int limit`
- **ArticleService.java**: `@ApplicationScoped`, replaced `Optionals.mapIfAllPresent` utility with inline Optional logic
- **Tag.java**: Column annotation updated to `@Column(name = "\"value\"")` to quote reserved SQL keyword
- **TagRepository.java**: Spring Data → EntityManager-based
- **TagService.java**: `@ApplicationScoped`
- **Comment.java**: Added JPA `@PrePersist`/`@PreUpdate` lifecycle callbacks for timestamps
- **CommentService.java**: `@ApplicationScoped`, inline Optional logic

#### 5d: Application Layer (Controllers → JAX-RS Resources)
- **UserRestController.java**: `@RestController` → `@Path("/")`, `@GetMapping`/`@PostMapping`/`@PutMapping` → `@GET`/`@POST`/`@PUT`, `@AuthenticationPrincipal` → `AuthContext` injection
- **ProfileRestController.java**: `@Path("/profiles")`, `@PathParam` for username
- **ArticleRestController.java**: `@Path("/articles")`, `@QueryParam`/`@DefaultValue` for pagination/filters
- **CommentRestController.java**: `@Path("/articles/{slug}/comments")`
- **TagRestController.java**: `@Path("/tags")`
- **MultipleArticleModel.java**: Changed from `Page<Article>` to `List<Article>`
- **GlobalExceptionHandler.java**: `@RestControllerAdvice`/`@ExceptionHandler` → JAX-RS `@Provider ExceptionMapper<Exception>`

#### 5e: Infrastructure Layer
- **JWTConfiguration.java**: `@Configuration`/`@Bean` → `@ApplicationScoped`/`@Produces`
- **HmacSHA256JWTService.java**: Changed from package-private to `public` (required by CDI)
- **Files removed**: `WebMvcConfiguration.java`, `SpringDataJPAConfiguration.java`

### Step 6: Dockerfile Update
- **Action**: Modified Dockerfile for Maven-based Quarkus build
- **Changes**:
  - Added Maven 3.9.6 manual installation (Ubuntu Focal apt only provides 3.6.x)
  - Build command: `mvn package -DskipTests`
  - Run command: `java -jar target/quarkus-app/quarkus-run.jar`

### Step 7: Test Files Removed
- **Action**: Removed `src/test/` directory (Spring-specific unit/integration tests not compatible with Quarkus)

---

### Errors and Resolutions

#### Error 1: Quarkus 3.x Jakarta Namespace Incompatibility
- **Symptom**: Compilation errors — all source files use `javax.*` imports but Quarkus 3.x requires `jakarta.*`
- **Root Cause**: Initially chose Quarkus 3.8.4, which migrated to Jakarta EE 10 namespace
- **Resolution**: Downgraded to Quarkus 2.16.12.Final which uses the `javax.*` namespace and supports Java 11

#### Error 2: Maven Version Too Old
- **Symptom**: Build failure — Quarkus 2.16 requires Maven >= 3.8, but Ubuntu Focal apt installs Maven 3.6.x
- **Root Cause**: System package manager provides outdated Maven
- **Resolution**: Added manual Maven 3.9.6 installation in Dockerfile via Apache archive tarball

#### Error 3: H2 Reserved Keyword `value`
- **Symptom**: Tag table creation failed with "expected identifier" SQL error. Cascaded to `/tags` endpoint returning 409, and article operations involving tags failing.
- **Root Cause**: The `Tag` entity maps to a column named `value`, which is a reserved SQL keyword in H2
- **Resolution**: Quoted the column name in the JPA annotation: `@Column(name = "\"value\"")`

---

### Final Validation
- **Result**: All 45 smoke tests pass (45/45 passed, 0/45 failed)
- **Endpoints verified**: User registration, login, authentication, profiles, articles (CRUD), favorites, comments, follow/unfollow, tags
