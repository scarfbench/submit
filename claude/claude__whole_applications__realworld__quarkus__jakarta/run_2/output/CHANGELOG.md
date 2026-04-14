# CHANGELOG: Quarkus to Standalone Jakarta EE Migration

## Summary

Migrated the RealWorld API application from **Quarkus 3.x** to **standalone Jakarta EE** using:
- **Undertow 2.3.18.Final** (embedded HTTP server)
- **RESTEasy 6.2.12.Final** (JAX-RS implementation)
- **Weld 5.1.3.Final** (CDI implementation)
- **Hibernate ORM 6.6.4.Final** (JPA implementation)
- **Nimbus JOSE JWT 9.47** (JWT token handling)
- **jBCrypt 0.4** (password hashing)

All 22 smoke tests pass: health checks, user CRUD, authentication, articles, comments, favorites, tags, profiles, and feed.

---

## Files Changed

### Build & Configuration

| File | Action | Description |
|------|--------|-------------|
| `pom.xml` | Modified | Replaced Quarkus BOM with standalone Jakarta EE dependencies; added Maven Shade plugin for fat JAR; added Jandex 3.2.0 with exclusions for old `org.jboss:jandex` |
| `Dockerfile` | Modified | Changed build command to `mvn clean package -DskipTests -Dmaven.test.skip=true`; added `ARG CACHE_BUST` to prevent BuildKit caching issues |
| `start.sh` | Modified | Changed from Quarkus runner JAR to shade fat JAR execution with `java -jar` |
| `src/main/resources/META-INF/beans.xml` | Modified | Changed `bean-discovery-mode` from `annotated` to `all`; added `TransactionalInterceptor` to `<interceptors>` |
| `src/main/resources/META-INF/persistence.xml` | Modified | Configured JDBC connection properties for PostgreSQL; changed `hibernate.hbm2ddl.auto` to `update` |

### New Files

| File | Description |
|------|-------------|
| `src/main/java/org/example/realworldapi/Main.java` | Application entry point using Weld SE + Undertow + RESTEasy with `CdiInjectorFactory` |
| `src/main/java/org/example/realworldapi/infrastructure/persistence/EntityManagerFactoryProducer.java` | CDI producer for `EntityManagerFactory` via JPA `Persistence.createEntityManagerFactory()` |
| `src/main/java/org/example/realworldapi/infrastructure/persistence/EntityManagerProducer.java` | CDI producer for `@RequestScoped EntityManager` with `@Disposes` cleanup |
| `src/main/java/org/example/realworldapi/infrastructure/persistence/TransactionalInterceptor.java` | CDI interceptor implementing `@Transactional` with RESOURCE_LOCAL transaction management |
| `src/main/java/org/example/realworldapi/infrastructure/web/security/JwtSecurityFilter.java` | JAX-RS `ContainerRequestFilter` for JWT authentication using Nimbus JOSE (supports both `Token` and `Bearer` schemes) |
| `src/main/java/org/example/realworldapi/infrastructure/web/security/JwtSecurityContext.java` | Custom `SecurityContext` implementation for JWT-authenticated requests |
| `src/main/java/org/example/realworldapi/infrastructure/web/security/RolesAllowedFilter.java` | JAX-RS `DynamicFeature` implementing `@RolesAllowed` authorization |
| `src/main/java/org/example/realworldapi/infrastructure/web/mapper/GenericExceptionMapper.java` | Exception mapper for `WebApplicationException` |
| `src/main/java/org/example/realworldapi/infrastructure/web/mapper/BusinessExceptionMapper.java` | Exception mapper for domain business exceptions |
| `src/main/java/org/example/realworldapi/infrastructure/web/mapper/BeanValidationExceptionMapper.java` | Exception mapper for `ConstraintViolationException` |
| `src/main/java/org/example/realworldapi/infrastructure/web/provider/JacksonConfigProvider.java` | Jackson `ObjectMapper` configuration provider |
| `src/main/java/org/example/realworldapi/infrastructure/provider/JwtTokenProvider.java` | JWT token creation using Nimbus JOSE with RSA signing |
| `src/main/java/org/example/realworldapi/application/web/resource/HealthResource.java` | Health/liveness/readiness endpoints |
| `smoke.py` | Comprehensive smoke test suite (22 tests) |

### Modified Files (Quarkus -> Jakarta EE)

| File | Changes |
|------|---------|
| `RestApplication.java` | Removed `getClasses()` override; resources registered via `ResteasyDeployment` in `Main.java` |
| `infrastructure/provider/SlugifySlugProvider.java` | Changed from `@AllArgsConstructor` to `@Inject` field injection for CDI proxy compatibility |
| `infrastructure/repository/hibernate/entity/EntityUtils.java` | Changed from `@AllArgsConstructor` to `@Inject` field injection for CDI proxy compatibility |
| `infrastructure/provider/HashProvider.java` | Replaced Quarkus `BcryptUtil` with `org.mindrot.jbcrypt.BCrypt` |
| `infrastructure/configuration/ApplicationConfiguration.java` | Added `@Produces @Singleton Validator` and `Slugify` producers |
| All resource classes (`UsersResource`, `UserResource`, `ArticlesResource`, `ProfilesResource`, `TagsResource`) | Added `@RequestScoped`; changed from `@AllArgsConstructor`/`final` fields to `@Inject` field injection; replaced Quarkus `@ServerExceptionMapper` with standard JAX-RS `ExceptionMapper` |
| All repository implementations | Replaced Panache queries with standard JPA `EntityManager` JPQL; replaced `Parameters`/`Page`/`Sort` with `setParameter`/`setFirstResult`/`setMaxResults` |

### Removed Quarkus-specific Code

- All `@RegisterForReflection` annotations
- All `io.quarkus.*` imports
- Quarkus Panache repository base classes
- SmallRye JWT / MicroProfile JWT dependencies
- `application.properties` Quarkus-specific settings (kept as placeholder)
- `@ServerExceptionMapper` annotations (replaced with `@Provider` ExceptionMapper classes)

---

## Key Technical Decisions

1. **Weld SE + CdiInjectorFactory**: Used Weld SE container initialized in `Main.main()` with RESTEasy's `CdiInjectorFactory` for CDI-JAX-RS integration. Added Weld Servlet `Listener` to the Undertow deployment for proper request scope activation.

2. **RESOURCE_LOCAL Transactions**: Used JPA `RESOURCE_LOCAL` transaction type with a custom `TransactionalInterceptor` CDI interceptor (since no JTA container is available). Transactions are managed via `EntityManager.getTransaction()`.

3. **Jandex Version Management**: Hibernate 6.6.x requires SmallRye Jandex 3.x, while RESTEasy 6.2.x transitively pulls in old `org.jboss:jandex:2.x`. Resolved by explicitly declaring `io.smallrye:jandex:3.2.0` and excluding `org.jboss:jandex` from all RESTEasy and Hibernate dependencies.

4. **CDI Bean Discovery**: Set `bean-discovery-mode="all"` in `beans.xml` to ensure all classes (including those without explicit CDI annotations) are discovered by Weld.

5. **Fat JAR via Maven Shade**: Used Maven Shade plugin with `ServicesResourceTransformer` (for SPI files) and `AppendingTransformer` (for `beans.xml` merging) to create a single executable JAR.

---

## Issues Encountered and Resolved

1. **`NoSuchMethodError: Indexer.indexWithSummary`**: Old Jandex 2.x classes conflicting with Hibernate's requirement for Jandex 3.x. Fixed with explicit dependency + exclusions.

2. **`Cannot place qualifiers on final fields`**: Lombok `@AllArgsConstructor` generated final fields with `@Inject` annotations, which Weld rejects. Fixed by switching to `@Inject` field injection (non-final).

3. **CDI Request Scope not active**: Resources annotated `@RequestScoped` failed because Weld SE doesn't automatically activate request scope. Fixed by adding `org.jboss.weld.environment.servlet.Listener` to the Undertow `DeploymentInfo`.

4. **RESTEasy resource auto-discovery not working**: With empty `RestApplication.getClasses()`, RESTEasy couldn't find resources. Fixed by explicitly registering resources via `deployment.setScannedResourceClasses()` in `Main.java`.

5. **Jackson WRAP_ROOT_VALUE on Maps**: Health endpoint returned `{"Map1":{"status":"UP"}}`. Fixed by using raw JSON string responses.

6. **Docker BuildKit caching stale files**: BuildKit reused old COPY layers despite `--no-cache`. Fixed by adding `ARG CACHE_BUST` before `COPY . .` and passing `--build-arg CACHE_BUST=$(date +%s)`.

---

## Smoke Test Results

```
=== Running RealWorld API Smoke Tests ===

  PASS: Health check
  PASS: Health live
  PASS: Health ready
  PASS: Register user
  PASS: Register duplicate user
  PASS: Login user
  PASS: Login with wrong password
  PASS: Get current user
  PASS: Update user
  PASS: Get profile
  PASS: Follow/unfollow user
  PASS: Create article
  PASS: Get article by slug
  PASS: List articles
  PASS: Update article
  PASS: Delete article
  PASS: Add comment
  PASS: Get comments
  PASS: Favorite/unfavorite article
  PASS: Get tags
  PASS: Articles feed
  PASS: Unauthenticated access returns 401

=== Results: 22 passed, 0 failed ===
```
