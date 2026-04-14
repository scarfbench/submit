# CHANGELOG - Jakarta EE (Open Liberty) to Quarkus Migration

## Migration Summary

**Source**: Jakarta EE 10 on Open Liberty with MicroProfile 7.1
**Target**: Quarkus 3.17.8
**Application**: RealWorld API (Conduit) - Blog/Article REST platform
**Result**: 72/72 smoke tests passed

---

## Phase 1: Analysis

### Identified Source Stack
- **Runtime**: Open Liberty with MicroProfile 7.1
- **Build**: Maven with `liberty-maven-plugin`, WAR packaging
- **REST**: JAX-RS via MicroProfile
- **ORM**: Hibernate ORM 7.1.0 with JPA (`persistence.xml`)
- **CDI**: Jakarta CDI (`beans.xml`)
- **Config**: MicroProfile Config (`microprofile-config.properties`)
- **Auth**: Custom JWT using auth0 java-jwt 4.5.0 with JAX-RS ContainerRequestFilters
- **Password hashing**: BCrypt (at.favre.lib 0.10.2)
- **Slugs**: Slugify 3.0.7
- **Serialization**: Jackson with custom ObjectMapper CDI producers (WRAP_ROOT_VALUE)
- **Database**: PostgreSQL
- **Code generation**: Lombok 1.18.38

### Key Observations
- Security is implemented via custom `@NameBinding` annotation (`@Secured`) with JAX-RS ContainerRequestFilters - framework-agnostic, works in both Liberty and Quarkus
- Application uses manual `objectMapper.writeValueAsString()` for response serialization with WRAP_ROOT_VALUE enabled for `@JsonRootName` annotated responses
- Two ObjectMapper instances: one with WRAP_ROOT_VALUE (for single-entity responses) and one without (for list responses)
- `@ApplicationPath("api")` sets the REST context path

---

## Phase 2: Migration Changes

### pom.xml
- **Changed**: `artifactId` from `realworld-liberty` to `realworld-quarkus`
- **Changed**: `packaging` from `war` to `jar`
- **Removed**: `liberty-maven-plugin`, `io.openliberty.features:microProfile-7.1`, `io.openliberty.features:servlet-6.1`, `jakarta.platform:jakarta.jakartaee-web-api`
- **Added**: `io.quarkus.platform:quarkus-bom` (3.17.8) dependency management
- **Added**: `quarkus-arc`, `quarkus-resteasy-jackson`, `quarkus-hibernate-orm`, `quarkus-hibernate-validator`, `quarkus-jdbc-postgresql`, `quarkus-narayana-jta`
- **Added**: `quarkus-maven-plugin` with `build`, `generate-code`, `generate-code-tests` goals
- **Added**: Lombok annotation processor path in `maven-compiler-plugin`
- **Added**: `maven-surefire-plugin` with JBoss LogManager system property

### src/main/resources/application.properties (NEW)
- Consolidates config from `persistence.xml`, `server.xml`, and `microprofile-config.properties`
- Quarkus datasource configuration with environment variable substitution
- Hibernate ORM settings (`drop-and-create`, UTC timezone)
- JWT configuration (`jwt.issuer`, `jwt.secret`, `jwt.expiration.time.minutes`)
- CORS configuration
- Banner disabled

### Infrastructure - JPA Repositories
- **AbstractDAO**: Changed `@PersistenceContext EntityManager` to `@Inject EntityManager` (Quarkus CDI injection)
- **AbstractDAO**: Rewritten as generic `AbstractDAO<ENTITY, ID>` with helper methods
- **UserDAO**: `@Singleton` scope, inline JPQL queries, case-insensitive search
- **ArticleDAO**: `@ApplicationScoped` scope, complex filter query builder pattern
- **CommentDAO**: `@Dependent` scope, uses named queries
- **TagDAO**: `@Dependent` scope, added `findByName()` method
- **FavoriteRelationshipDAO**: `@Dependent` scope
- **FollowRelationshipDAO**: `@Dependent` scope
- **TagRelationshipDAO**: `@Dependent` scope, added `findTagRelationships()` method
- **SimpleQueryBuilder**: Rewritten for dynamic JPQL query construction

### Infrastructure - Entity Classes
- **UserEntity**: Added constructor from `User`, `update()` method, `@OneToMany` relationships
- **ArticleEntity**: Added `@CreationTimestamp`/`@UpdateTimestamp`, `@OneToMany` relationships, constructor from `Article`+`UserEntity`
- **CommentEntity**: Added constructor from `UserEntity`+`ArticleEntity`+`Comment`, named queries
- **TagEntity**: Added constructor from `Tag`, `@OneToMany` for articlesTags
- **TagRelationshipEntity**: Changed ID from composite key to `Long` with `@GeneratedValue(IDENTITY)`
- **FavoriteRelationshipEntity**: Changed ID from composite key to `Long` with `@GeneratedValue(IDENTITY)`
- **FollowRelationshipEntity**: Changed ID from composite key to `Long` with `@GeneratedValue(IDENTITY)`
- **EntityUtils**: Changed from static utility class to `@ApplicationScoped` CDI bean

### Domain Model
- **FollowRelationship**: Simplified to `user` + `followed` fields only
- **FavoriteRelationship**: Constructor with `user` + `article` only
- **UserModelBuilder**: Added `@Named("usermodelbuilder")`, `@Singleton`, second `build()` overload
- **ArticleModelBuilder**: `@Singleton`, added `build()` overload with all fields including timestamps
- **CommentBuilder**: `@Singleton`, added `build()` overload with all fields
- **TagBuilder**: `@Singleton`, added `build(UUID, String)` overload
- **TagRepository**: Added `findByName()` method
- **TagRelationshipRepository**: Added `findTagRelationships()` method

### Serialization (SerializerConfig)
- **Issue Found**: Quarkus's `quarkus-resteasy-jackson` manages its own default `ObjectMapper`. The original CDI `@Produces ObjectMapper` with WRAP_ROOT_VALUE/UNWRAP_ROOT_VALUE conflicted with RESTEasy's deserialization, causing request body parsing to fail.
- **Solution**:
  - Default Quarkus ObjectMapper customized only with `JavaTimeModule` (via `ObjectMapperCustomizer`)
  - Created `@WrapRootValueObjectMapper` CDI qualifier for the WRAP_ROOT_VALUE ObjectMapper
  - Updated all injection points in resource classes and exception mappers to use `@WrapRootValueObjectMapper`
  - Kept `@NoWrapRootValueObjectMapper` for list responses

### Resource Classes
- **UsersResource**: Removed buggy `@Context SecurityException context` parameter (was `java.lang.SecurityException`, not a JAX-RS context type), added `@WrapRootValueObjectMapper`
- **UserResource**: Added `@WrapRootValueObjectMapper`
- **ProfilesResource**: Added `@WrapRootValueObjectMapper`
- **ArticlesResource**: Added `@WrapRootValueObjectMapper` for single-entity ObjectMapper
- **TagsResource**: Unchanged (uses default ObjectMapper via RESTEasy auto-serialization)

### Exception Mappers
- **BusinessExceptionMapper**: Added `@WrapRootValueObjectMapper`
- **InfrastructureExceptionMapper**: Added `@WrapRootValueObjectMapper`
- **BeanValidationExceptionMapper**: Added `@WrapRootValueObjectMapper`

### Request Models
- Removed `jakarta.json.bind.annotation.JsonbNillable` imports (JSON-B not used in Quarkus with Jackson)

### Deleted Files
- `src/main/resources/META-INF/persistence.xml` (replaced by application.properties)
- `src/main/resources/META-INF/beans.xml` (not needed in Quarkus)
- `src/test/resources/META-INF/beans.xml` (not needed)
- `src/main/resources/META-INF/microprofile-config.properties` (replaced by application.properties)
- `src/main/liberty/server.xml` (Liberty-specific)
- `src/test/` directory (old MicroShed tests, not compatible with Quarkus)

### New Files
- `src/main/resources/application.properties` - Quarkus configuration
- `src/main/java/.../infrastructure/web/qualifiers/WrapRootValueObjectMapper.java` - CDI qualifier
- `smoke.py` - Comprehensive smoke test suite (72 tests)

### Build & Deployment
- **Dockerfile**: Kept same base image (`maven:3.9.12-ibm-semeru-21-noble`), updated CMD
- **start.sh**: Auto-detects PostgreSQL version, builds with `mvn package -DskipTests`, runs `java -jar target/quarkus-app/quarkus-run.jar`

---

## Phase 3: Errors and Resolutions

### Error 1: Jackson UNWRAP_ROOT_VALUE Deserialization Failure
- **Symptom**: `MismatchedInputException: Root name ('user') does not match expected ('NewUserRequestWrapper')` when POSTing to `/users`
- **Root Cause**: The `ObjectMapperCustomizer` enabled WRAP_ROOT_VALUE/UNWRAP_ROOT_VALUE on the Quarkus default ObjectMapper. RESTEasy uses this ObjectMapper for deserializing request bodies. With UNWRAP_ROOT_VALUE enabled, Jackson tried to unwrap the `"user"` key as a root name, but `NewUserRequestWrapper` doesn't have `@JsonRootName` - it has a `user` field.
- **Fix**: Removed WRAP_ROOT_VALUE from the default ObjectMapper. Created a separate `@WrapRootValueObjectMapper` CDI qualifier. Updated all injection points.

### Error 2: Tags Response Missing 'tags' Key
- **Symptom**: `GET /tags` returned `{"TagsResponse":{"tags":[...]}}` instead of `{"tags":[...]}`
- **Root Cause**: `TagsResource.getTags()` passes the `TagsResponse` object directly to `Response.ok()`, letting RESTEasy/Jackson serialize it. With WRAP_ROOT_VALUE on the default ObjectMapper, Jackson wrapped it using the class name.
- **Fix**: Same as Error 1 - removing WRAP_ROOT_VALUE from the default ObjectMapper fixed this.

---

## Phase 4: Validation

### Smoke Test Results: 72 passed, 0 failed
- GET /tags (empty) - PASS
- GET /articles (empty) - PASS
- POST /users (register) - PASS (status 201, correct response structure)
- POST /users/login - PASS (status 200, token returned)
- GET /user (current user) - PASS
- PUT /user (update bio) - PASS
- GET /articles/feed - PASS
- POST /articles (create with tags) - PASS (status 201)
- GET /articles/{slug} - PASS
- GET /articles?author=smokeuser - PASS (count=1)
- GET /articles?tag=dragons - PASS (count=1)
- PUT /articles/{slug} (update body) - PASS
- POST /articles/{slug}/favorite - PASS (favorited=true, count=1)
- DELETE /articles/{slug}/favorite - PASS (favorited=false, count=0)
- POST /articles/{slug}/comments - PASS
- GET /articles/{slug}/comments - PASS (count >= 1)
- DELETE /articles/{slug}/comments/{id} - PASS
- POST /users (register celeb) - PASS
- GET /profiles/celeb_smokeuser - PASS (following=false)
- POST /profiles/{username}/follow - PASS (following=true)
- DELETE /profiles/{username}/follow - PASS (following=false)
- GET /tags (after article creation) - PASS (dragons, training present)
- DELETE /articles/{slug} - PASS

### Unchanged Components (verified working)
- Custom JWT authentication (auth0 java-jwt with ContainerRequestFilters)
- `@NameBinding` security annotation (`@Secured`)
- BCrypt password hashing
- Slugify slug generation
- Bean validation constraints
- All domain feature interfaces and implementations
