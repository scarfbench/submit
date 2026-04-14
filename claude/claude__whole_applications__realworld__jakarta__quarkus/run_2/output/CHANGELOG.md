# Migration Changelog: Jakarta EE (Open Liberty) -> Quarkus

## [2026-03-14T07:00:00Z] [info] Project Analysis
- Identified Jakarta EE / MicroProfile / Open Liberty application (RealWorld "Conduit" API)
- 162 Java source files, domain-driven design architecture
- Features: User registration/login (JWT), articles, comments, tags, profiles, favorites
- Dependencies: MicroProfile 7.1, Hibernate ORM 7.1, Jackson, BCrypt, Slugify, Lombok
- Server: Open Liberty with PostgreSQL
- Build: Maven with liberty-maven-plugin

## [2026-03-14T07:01:00Z] [info] Smoke Test Generation
- Created smoke.sh with 14 comprehensive tests covering:
  - GET /api/tags (unauthenticated)
  - GET /api/articles (unauthenticated)
  - POST /api/users (registration)
  - POST /api/users/login (authentication)
  - GET /api/user (authenticated)
  - PUT /api/user (update profile)
  - POST /api/articles (create article)
  - GET /api/articles/{slug} (get article)
  - POST /api/articles/{slug}/comments (add comment)
  - GET /api/articles/{slug}/comments (list comments)
  - POST /api/articles/{slug}/favorite (favorite article)
  - GET /api/profiles/{username} (get profile)
  - POST /api/users/login with invalid credentials (error handling)
  - GET /api/user unauthenticated (401 check)

## [2026-03-14T07:02:00Z] [info] Dependency Migration (pom.xml)
- Changed packaging from `war` to `jar`
- Changed artifactId from `realworld-liberty` to `realworld-quarkus`
- Removed: MicroProfile 7.1 BOM, Hibernate ORM 7.1 direct dependency, Hibernate Validator direct,
  expressly (EL), org.json, PostgreSQL JDBC driver (direct), Jackson (direct), weld-junit5,
  microshed-testing-core-jakarta, microshed-testing-liberty, testcontainers, reflections, gson
- Added: Quarkus BOM 3.17.8, quarkus-rest-jackson, quarkus-hibernate-orm, quarkus-jdbc-postgresql,
  quarkus-hibernate-validator, quarkus-arc, quarkus-narayana-jta, quarkus-smallrye-health,
  quarkus-junit5, jackson-datatype-jsr310
- Replaced: liberty-maven-plugin with quarkus-maven-plugin
- Retained: java-jwt 4.5.0, bcrypt 0.10.2, slugify 3.0.7, commons-lang3, lombok, rest-assured

## [2026-03-14T07:03:00Z] [info] Configuration Migration
- Created src/main/resources/application.properties for Quarkus configuration
- Configured: datasource (PostgreSQL), Hibernate ORM (drop-and-create), REST path (/api),
  JWT properties, CORS settings
- Removed: src/main/resources/META-INF/persistence.xml (Quarkus uses application.properties)
- Removed: src/main/resources/META-INF/microprofile-config.properties (migrated to application.properties)
- Removed: src/main/resources/META-INF/beans.xml (not needed in Quarkus)
- Removed: src/main/liberty/config/server.xml (Liberty-specific)

## [2026-03-14T07:04:00Z] [info] Code Refactoring - EntityManager Injection
- File: AbstractDAO.java
- Changed: @PersistenceContext -> @Inject for EntityManager injection
- Reason: Quarkus provides EntityManager via CDI @Inject, not JPA @PersistenceContext

## [2026-03-14T07:05:00Z] [info] Code Refactoring - ObjectMapper Configuration
- Replaced CDI @Produces ObjectMapper beans with SerializerConfig service class
- Reason: CDI-produced ObjectMapper beans interfered with Quarkus's default ObjectMapper
  used by quarkus-rest-jackson for request/response serialization
- Created QuarkusObjectMapperConfig.java (ObjectMapperCustomizer for JavaTimeModule)
- SerializerConfig now provides getWrapRootValueMapper() and getNoWrapRootValueMapper() methods
- Updated 7 files to use SerializerConfig instead of direct ObjectMapper injection:
  ArticlesResource, UserResource, UsersResource, ProfilesResource,
  BusinessExceptionMapper, BeanValidationExceptionMapper, InfrastructureExceptionMapper

## [2026-03-14T07:06:00Z] [info] Code Refactoring - Resource Class Scoping
- Added @RequestScoped annotation to all JAX-RS resource classes:
  ArticlesResource, UserResource, UsersResource, ProfilesResource, TagsResource
- Reason: Quarkus RESTEasy Reactive requires explicit CDI scope for field injection

## [2026-03-14T07:07:00Z] [info] Code Refactoring - JSON-B to Jackson Migration
- Removed unused jakarta.json.bind.annotation.JsonbNillable imports from:
  NewArticleRequest.java, NewCommentRequest.java, UpdateArticleRequest.java
- Reason: Quarkus uses Jackson (not JSON-B) for serialization with quarkus-rest-jackson

## [2026-03-14T07:07:30Z] [info] Code Refactoring - Method Parameter Fix
- File: UsersResource.java
- Removed unused @Context SecurityException parameter from create() method
- Reason: SecurityException is not a valid JAX-RS @Context type and caused 400 errors
  in RESTEasy Reactive (Liberty was more lenient about this)

## [2026-03-14T07:08:00Z] [error] Compilation Failure - JSON-B Imports
- Error: package jakarta.json.bind.annotation does not exist
- Files: NewArticleRequest.java, NewCommentRequest.java, UpdateArticleRequest.java
- Root Cause: JSON-B annotations imported but unused; JSON-B not included in Quarkus dependencies
- Resolution: Removed unused imports

## [2026-03-14T07:09:00Z] [error] Runtime Deserialization Failure
- Error: Root name ('user') does not match expected ('NewUserRequestWrapper')
- Root Cause: CDI-produced ObjectMapper with UNWRAP_ROOT_VALUE was being picked up by
  Quarkus as the default ObjectMapper for REST serialization
- Resolution: Replaced CDI @Produces ObjectMapper with SerializerConfig service bean
  pattern to avoid interfering with Quarkus's default ObjectMapper

## [2026-03-14T07:10:00Z] [info] Test Infrastructure
- Removed Liberty-specific integration tests (src/test/) that depended on:
  microshed-testing-liberty, LibertyContainer, AppDeploymentConfig
- Replaced with smoke.sh script for external HTTP-based testing

## [2026-03-14T07:11:00Z] [info] Dockerfile and Start Script Updates
- Updated Dockerfile: Added `mvn package -DskipTests -Dquarkus.package.jar.type=uber-jar` build step
- Updated start.sh: Changed from `mvn clean liberty:run` to `java -jar /app/target/realworld-quarkus-runner.jar`
- Added PostgreSQL wait loop in start.sh for reliable startup

## [2026-03-14T07:15:00Z] [info] Build Success
- Docker image built successfully
- Quarkus application starts in ~4 seconds
- Installed features: agroal, cdi, hibernate-orm, hibernate-validator, jdbc-postgresql,
  narayana-jta, rest, rest-jackson, smallrye-context-propagation, smallrye-health, vertx

## [2026-03-14T07:16:00Z] [info] Smoke Test Results - ALL PASSED
- Total: 14 | Passed: 14 | Failed: 0
- All API endpoints functional: tags, articles, users, profiles, comments, favorites
- Authentication (JWT) working correctly
- Validation and error handling working correctly
- Database persistence (PostgreSQL + Hibernate ORM) working correctly
