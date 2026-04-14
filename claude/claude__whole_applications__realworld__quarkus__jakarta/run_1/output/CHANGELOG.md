# Changelog

## Migration: Quarkus to Standalone Jakarta EE

### Summary

Migrated the RealWorld (Conduit) API application from Quarkus to a standalone Jakarta EE stack using RESTEasy + Weld CDI + Hibernate ORM + Undertow. The application is fully functional with all 67 smoke tests passing.

### Technology Stack

| Component | Before (Quarkus) | After (Standalone) |
|-----------|-------------------|---------------------|
| Framework | Quarkus 3.x | None (standalone) |
| JAX-RS | quarkus-resteasy-jackson | RESTEasy 6.2.12.Final |
| CDI | quarkus-arc | Weld SE 5.1.3.Final |
| JPA/ORM | quarkus-hibernate-orm-panache | Hibernate ORM 6.6.4.Final |
| HTTP Server | Vert.x (embedded) | Undertow 2.3.18.Final |
| JWT | quarkus-smallrye-jwt | Nimbus JOSE+JWT 9.47 |
| Password Hashing | Built-in | BCrypt (favre) 0.10.2 |
| Build | Quarkus Maven plugin | Maven Shade plugin |
| Database (dev) | Quarkus DevServices | H2 in-memory |

### Key Changes

#### Build System (pom.xml)
- Removed all `io.quarkus` dependencies and Quarkus BOM
- Added explicit dependencies: RESTEasy (core, undertow, jackson2-provider, cdi, validator-provider), Weld SE + Servlet, Hibernate ORM, Undertow (core, servlet), Jackson, Nimbus JOSE+JWT, BCrypt, Slugify, Bean Validation (Hibernate Validator + Expressly EL)
- Resolved jandex version conflict: excluded `org.jboss:jandex` 2.x from Weld, added `io.smallrye:jandex` 3.2.0 (required by Hibernate 6.x)
- Configured Maven Shade plugin for fat JAR with `ServicesResourceTransformer` and `AppendingTransformer` for beans.xml

#### Application Bootstrap (Main.java)
- Created `Main.java` with embedded Weld SE + Undertow + RESTEasy setup
- Weld SE initializes CDI container first, then RESTEasy deployment is configured with `CdiInjectorFactory`
- Resource and provider classes registered explicitly via `setScannedResourceClasses` / `setScannedProviderClasses`
- BeanManager passed to servlet context for CDI-JAX-RS integration

#### CDI Configuration
- All JAX-RS resources changed from `@RequestScoped` to `@Dependent` (Weld SE doesn't have servlet request context)
- All CDI beans converted from Lombok constructor injection to field injection (`@Inject private`) for Weld proxy compatibility
- `@ApplicationScoped` beans have `@NoArgsConstructor(force = true)` for Weld proxying
- Added `META-INF/beans.xml` with `bean-discovery-mode="annotated"` and `TransactionalInterceptor` registration

#### Persistence (EntityManager management)
- Created `EntityManagerProducer` with `ThreadLocal<EntityManager>` pattern
- Each HTTP request gets its own EntityManager via thread-local, shared across all repositories within the same request
- Repositories use `getEm()` method (from `AbstractPanacheRepository`) instead of `@Inject EntityManager em` field to avoid stale references in `@ApplicationScoped` beans
- `TransactionalInterceptor` manages resource-local transactions: begin/commit/rollback + cleanup via `closeCurrentEntityManager()`

#### Transaction Management
- Created `TransactionalInterceptor` CDI interceptor bound to `@jakarta.transaction.Transactional`
- All JAX-RS endpoint methods annotated with `@Transactional` (both read and write operations need it for EntityManager transaction context)
- Replaces Quarkus/Narayana JTA transaction management

#### Security
- Created `SecurityFilter` (`@Provider` `ContainerRequestFilter`) for JWT token validation using RSA public key
- Created `RolesAllowedFilter` for `@RolesAllowed`/`@PermitAll` annotation processing
- Created `JwtTokenProvider` using Nimbus JOSE+JWT for token creation with RSA key pair
- Created `BCryptHashProvider` using favre BCrypt library

#### Jackson/Serialization
- Created `ObjectMapperContextResolver` (`ContextResolver<ObjectMapper>`) to configure RESTEasy's Jackson with `WRAP_ROOT_VALUE` and `UNWRAP_ROOT_VALUE` (required by RealWorld API spec's `{"user": {...}}` envelope format)

#### Exception Handling
- `ExceptionMappers` handles `RuntimeException` subtypes (auth, not found, conflict, validation)
- `BeanValidationExceptionMapper` handles `ConstraintViolationException`
- Removed `@RegisterForReflection` annotations (Quarkus-specific)

#### Configuration
- Created `persistence.xml` with H2 in-memory database (`jdbc:h2:mem:realworld`)
- Created `META-INF/beans.xml` for CDI bean discovery
- RSA key pair (`privateKey.pem`, `publicKey.pem`) on classpath for JWT signing/verification

#### Dockerfile
- Multi-stage build: Maven build stage + JRE 21 runtime stage
- Fat JAR execution: `java -jar realworldapiservice-1.0-SNAPSHOT.jar`
- Exposed port 8080

### Testing

All 67 smoke tests pass, covering:
- User registration and login
- Current user retrieval and update
- Article CRUD (create, read, update, delete)
- Article filtering by author and tag
- Article favoriting/unfavoriting
- Comment CRUD
- User feed
- Profile retrieval, follow/unfollow
- Tags listing
