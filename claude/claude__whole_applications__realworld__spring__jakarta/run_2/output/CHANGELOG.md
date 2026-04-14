# Changelog

## 3.0.0 - Jakarta EE Migration (Spring Boot 3.x)

### Overview

Migrated the RealWorld (Conduit) Spring Boot application from Spring Boot 2.5.2 (javax namespace) to Spring Boot 3.2.5 (jakarta namespace), including all necessary dependency upgrades and API changes.

### Build & Infrastructure

- **Spring Boot**: 2.5.2 -> 3.2.5
- **Spring Dependency Management Plugin**: 1.0.11.RELEASE -> 1.1.5
- **Gradle Wrapper**: 6.8.3 -> 8.7
- **Java**: 11 -> 17
- **Dockerfile**: `eclipse-temurin:11-jdk-focal` -> `eclipse-temurin:17-jdk-focal`
- **mockito-inline**: 3.12.1 -> 5.2.0
- Removed unused plugins: sonarqube, editorconfig, jib

### Namespace Migration (javax -> jakarta)

- `javax.persistence.*` -> `jakarta.persistence.*` in all JPA entity classes:
  - Article, ArticleContents, ArticleTitle, Tag, Comment, User, Profile, Email, UserName, Password, Image
- `javax.validation.*` -> `jakarta.validation.*` in all DTOs and controllers:
  - ArticlePostRequestDTO, CommentPostRequestDTO, UserPostRequestDTO, UserLoginRequestDTO
  - ArticleRestController, CommentRestController, UserRestController
- `javax.servlet.*` -> `jakarta.servlet.*` in JWTAuthenticationFilter
- Note: `javax.crypto.*` (JDK package, not Jakarta EE) was intentionally left unchanged

### Spring Security 6 Migration

- Removed `WebSecurityConfigurerAdapter` (deleted in Spring Security 6)
- Replaced override-based configuration with `@Bean SecurityFilterChain` and `@Bean WebSecurityCustomizer`
- Migrated to lambda DSL for all security configuration (csrf, cors, formLogin, logout, exceptionHandling)
- `antMatchers()` -> `requestMatchers()` with MVC pattern matching
- `authorizeRequests()` -> `authorizeHttpRequests()`
- Updated path patterns for Spring Security 6 MVC matching:
  - `/profiles/*` -> `/profiles/{username}` (MVC-style path variables)
  - Added explicit base paths alongside wildcards (e.g., `/tags` and `/tags/**`)
- JWTAuthenticationFilter now uses `AuthenticationManager` to properly authenticate JWT tokens
  (Spring Security 6 requires explicit authentication via AuthenticationManager; simply setting
  an unauthenticated token in the SecurityContext is no longer sufficient)
- Exposed `AuthenticationManager` as a bean via `AuthenticationConfiguration`
- Removed `@ConstructorBinding` from `SecurityConfigurationProperties` (inferred for single-constructor classes in Spring Boot 3)

### Spring Data 3.x Migration

- Replaced `Optionals.mapIfAllPresent()` (removed in Spring Data 3.x) with `flatMap`/`map` chains in:
  - `ArticleService` (3 occurrences)
  - `CommentService` (2 occurrences)

### H2 2.x Compatibility

- Quoted reserved keyword `value` in schema.sql (`value` -> `"value"`) for the tags table
- Added `@Column(name = "\"value\"")` to the Tag entity to generate quoted SQL identifiers
- Changed `VARCHAR` (without length) to `TEXT` for article and comment body columns

### Gradle 8 Compatibility

- JaCoCo report config: `html.enabled` -> `html.required`, `xml.enabled` -> `xml.required` (in test.gradle)

### Configuration

- Added `spring.sql.init.mode=always` to application.properties and application-docker.properties
- Added `application-docker.properties` profile with actuator health endpoint configuration

### Testing

- Added comprehensive HTTP smoke test suite (`smoke.py`) covering:
  - Tags listing
  - User registration and login
  - Current user retrieval and update
  - Profile viewing
  - Follow/unfollow users
  - Article CRUD (create, read, update, delete)
  - Comments CRUD (create, read, delete)
  - Article favorites/unfavorites
  - 35 test assertions, all passing
