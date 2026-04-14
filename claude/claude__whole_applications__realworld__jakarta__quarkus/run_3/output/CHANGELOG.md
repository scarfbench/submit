# CHANGELOG - Jakarta EE (Open Liberty) to Quarkus Migration

## Migration Summary

Migrated the RealWorld API (Conduit) application from Jakarta EE 10 / MicroProfile 7.1 on Open Liberty to Quarkus 3.17.8. The application is a Medium.com clone implementing the RealWorld spec with users, articles, comments, tags, profiles, favorites, and follow functionality.

## Changes

### Dependencies (pom.xml)

- Changed packaging from `war` to `jar`
- Changed artifactId from `realworld-liberty` to `realworld-quarkus`
- Added Quarkus BOM (`io.quarkus.platform:quarkus-bom:3.17.8`) as dependency management
- Replaced Jakarta/Liberty dependencies with Quarkus extensions:
  - `quarkus-resteasy-jackson` (replaces jakarta.platform:jakartaee-api + org.eclipse.microprofile:microprofile)
  - `quarkus-hibernate-orm` (replaces org.hibernate.orm:hibernate-core)
  - `quarkus-jdbc-postgresql` (replaces org.postgresql:postgresql)
  - `quarkus-hibernate-validator` (replaces org.hibernate.validator:hibernate-validator + org.glassfish.expressly:expressly)
  - `quarkus-arc` (CDI container)
  - `quarkus-smallrye-health` (health checks)
  - `quarkus-junit5` + `rest-assured` (test dependencies)
- Removed Liberty-specific dependencies: `io.openliberty.tools:liberty-maven-plugin`, `org.microshed:microshed-testing-liberty`, `org.testcontainers`, `org.json:json`
- Removed standalone `jackson-databind` and `jackson-datatype-jsr310` (provided by Quarkus)
- Kept: `com.auth0:java-jwt`, `at.favre.lib:bcrypt`, `com.github.slugify:slugify`, `commons-lang3`, `lombok`
- Replaced `maven-war-plugin` + `liberty-maven-plugin` + `maven-failsafe-plugin` with `quarkus-maven-plugin`

### Configuration

- **Created** `src/main/resources/application.properties` with Quarkus configuration:
  - Datasource: PostgreSQL with environment variable substitution
  - Hibernate ORM: `drop-and-create` schema generation
  - JWT: custom config properties (issuer, secret, expiration)
  - CORS: permissive configuration for development
- **Removed** `src/main/resources/META-INF/persistence.xml` (replaced by application.properties)
- **Removed** `src/main/resources/META-INF/beans.xml` (Quarkus Arc doesn't require it)
- **Removed** `src/main/resources/META-INF/microprofile-config.properties` (merged into application.properties)
- **Removed** `src/main/liberty/config/server.xml` (Liberty-specific)

### CDI / Dependency Injection Changes

- Changed `@PersistenceContext` to `@Inject` for `EntityManager` in `AbstractDAO.java`
- Removed `private` modifier from all `@Inject` fields across the codebase (Quarkus Arc CDI-lite requirement). Affected files:
  - All resource classes (UsersResource, UserResource, ArticlesResource, ProfilesResource, TagsResource)
  - All DAO classes (ArticleDAO, UserDAO, CommentDAO, FavoriteRelationshipDAO, FollowRelationshipDAO, TagDAO, TagRelationshipDAO)
  - All domain feature implementation classes (29 files in domain/feature/impl/)
  - All builder classes (ArticleModelBuilder, UserModelBuilder, CommentBuilder, TagBuilder)
  - EntityUtils, ResourceUtils, ModelValidator
  - Security filters (AuthenticationFilter, AuthorizationFilter)
  - Exception mappers (BusinessExceptionMapper, InfrastructureExceptionMapper, BeanValidationExceptionMapper)

### ObjectMapper / Jackson Serialization

- **Refactored** `SerializerConfig.java`: Removed CDI `@Produces` ObjectMapper beans to prevent conflict with Quarkus's built-in RESTEasy Jackson ObjectMapper. The wrapping ObjectMapper (with `WRAP_ROOT_VALUE`/`UNWRAP_ROOT_VALUE`) and the no-wrap ObjectMapper are now managed as plain fields with getter methods.
- **Created** `ObjectMapperConfig.java` implementing `io.quarkus.jackson.ObjectMapperCustomizer` to register `JavaTimeModule` on Quarkus's default ObjectMapper for `LocalDateTime` serialization.
- Updated all resource classes and exception mappers to inject `SerializerConfig` instead of `ObjectMapper` directly, calling `getWrappingObjectMapper()` or `getNoWrapRootValueObjectMapper()` as needed.

### Code Fixes

- Removed unused `@Context SecurityException context` parameter from `UsersResource.create()` method
- Removed unused `jakarta.json.bind.annotation.JsonbNillable` imports from `NewArticleRequest`, `NewCommentRequest`, `UpdateArticleRequest`

### Infrastructure

- **Updated** `Dockerfile`: Changed base image from `maven:3.9.12-ibm-semeru-21-noble` to `maven:3.9.12-eclipse-temurin-21-noble`; added `mvn package` build step
- **Updated** `start.sh`: Changed from `mvn liberty:run` to `java -jar target/quarkus-app/quarkus-run.jar`; removed `set -e` to prevent container exit on test failure; added PostgreSQL setup, app readiness check, and smoke test execution
- **Created** `smoke.py`: Python smoke test covering 58 test cases across all API endpoints (health, users, login, articles, comments, tags, profiles, favorites, follow/unfollow, feed, unauthorized access)
- **Removed** `src/test/` directory (contained Liberty/MicroShed-specific tests)

## Test Results

All 58 smoke tests passed:
- Health check: 1 test
- Unauthorized access: 2 tests
- User registration: 5 tests
- User login: 3 tests
- Get current user: 4 tests
- Update user: 2 tests
- Tags: 2 tests
- Profiles: 3 tests
- Follow/unfollow: 4 tests
- Create article: 8 tests
- Get article: 2 tests
- List articles: 3 tests
- Update article: 2 tests
- Favorite/unfavorite: 5 tests
- Comments (add/get/delete): 8 tests
- Article feed: 2 tests
- Delete article: 2 tests

## Issues Encountered and Resolved

1. **Compilation errors**: `package jakarta.json.bind.annotation does not exist` - resolved by removing unused JSON-B imports
2. **CDI bean conflict**: Quarkus RESTEasy Jackson provider picked up the CDI-produced wrapping ObjectMapper (with `UNWRAP_ROOT_VALUE` enabled) for request deserialization, causing HTTP 400 errors on all POST endpoints. Resolved by removing `@Produces` ObjectMapper beans from CDI and managing them as plain objects via `SerializerConfig`.
3. **RESTEasy variant**: Initially used `quarkus-rest-jackson` (RESTEasy Reactive) but switched to `quarkus-resteasy-jackson` (RESTEasy Classic) for better compatibility with traditional JAX-RS patterns (`@NameBinding`, `@Context ResourceInfo`, `ContainerRequestFilter`).
