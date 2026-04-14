# CHANGELOG - Spring Boot 2.x to 3.x (Jakarta) Migration

## Version 3.0.0

### Summary
Complete migration from Spring Boot 2.5.2 (javax namespace) to Spring Boot 3.2.5 (jakarta namespace), including Java 17, Gradle 8.7, Spring Security 6, Hibernate 6, and H2 2.x compatibility.

### Build System Changes

#### build.gradle
- Spring Boot plugin: `2.5.2` -> `3.2.5`
- Spring Dependency Management plugin: `1.0.11.RELEASE` -> `1.1.5`
- Java source compatibility: `11` -> `17`
- Application version: `0.0.1-SNAPSHOT` -> `3.0.0`
- Lombok version: `1.18.20` -> `1.18.36`
- Removed plugins: `sonarqube`, `editorconfig`, `jib`
- Removed dependency: `mockito-inline` (merged into mockito-core in newer versions)

#### gradle/wrapper/gradle-wrapper.properties
- Gradle distribution: `6.8.3` -> `8.7`

#### test.gradle
- JaCoCo report API: `html.enabled true` -> `html.required = true`
- JaCoCo report API: `xml.enabled true` -> `xml.required = true`

#### Dockerfile
- Converted from single-stage to multi-stage build
- Builder stage: `eclipse-temurin:17-jdk-jammy` (compiles JAR)
- Runtime stage: `eclipse-temurin:17-jre-jammy` (runs app)
- Added python3 and curl for smoke testing

### Namespace Migration (javax -> jakarta)

#### Entity Classes (javax.persistence -> jakarta.persistence)
- `Article.java`
- `ArticleContents.java`
- `ArticleTitle.java`
- `Comment.java`
- `Tag.java`
- `User.java`
- `Profile.java`
- `Email.java`
- `Password.java`
- `Image.java`
- `UserName.java`

#### Validation DTOs (javax.validation -> jakarta.validation)
- `ArticlePostRequestDTO.java`
- `CommentPostRequestDTO.java`
- `UserPostRequestDTO.java`
- `UserLoginRequestDTO.java`
- `ArticleRestController.java`
- `CommentRestController.java`
- `UserRestController.java`

#### Servlet API (javax.servlet -> jakarta.servlet)
- `JWTAuthenticationFilter.java`

### Spring Security 6 Migration

#### SecurityConfiguration.java (Major Rewrite)
- Removed `WebSecurityConfigurerAdapter` (deleted in Spring Security 6)
- Replaced `configure(HttpSecurity)` override with `@Bean SecurityFilterChain`
- Replaced `antMatchers()` with `requestMatchers()` (Spring Security 6 path matching)
- Replaced `authorizeRequests()` with `authorizeHttpRequests()`
- Used lambda-style DSL for all security configurers (`csrf`, `cors`, `formLogin`, `logout`, `exceptionHandling`)
- Removed `@ConstructorBinding` from `SecurityConfigurationProperties` (no longer needed for constructor-based binding)
- Made `SecurityConfigurationProperties` a separate package-private class

#### JWTAuthenticationFilter.java (Spring Security 6 Compatibility)
- Added `AuthenticationManager` injection via constructor
- Filter now explicitly authenticates JWT tokens through `AuthenticationManager.authenticate()`
- In Spring Security 5, setting an unauthenticated token on the SecurityContext worked because `FilterSecurityInterceptor` would invoke the AuthenticationManager
- In Spring Security 6, `AuthorizationFilter` replaced `FilterSecurityInterceptor` and only checks `isAuthenticated()` without triggering authentication
- Added `startsWith("Token ")` safety check before substring extraction
- Added try/catch for `AuthenticationException` to gracefully handle invalid tokens

#### SecurityConfiguration.java (AuthenticationManager Bean)
- Created `@Bean AuthenticationManager jwtAuthenticationManager` using `ProviderManager`
- Replaced `@Bean JWTAuthenticationProvider` with inline provider creation in `ProviderManager`
- `AuthenticationManager` is injected into `SecurityFilterChain` and passed to `JWTAuthenticationFilter`

### Spring Data 3 Migration

#### ArticleService.java, CommentService.java
- Replaced `org.springframework.data.util.Optionals.mapIfAllPresent` (removed in Spring Data 3)
- Added local `mapIfAllPresent` helper method using `Optional.flatMap`/`Optional.map`

### H2 Database 2.x Compatibility

#### schema.sql
- Quoted `"value"` column name in `tags` table (H2 2.x treats `value` as a reserved keyword)

#### Tag.java
- Changed `@Column(name = "value")` to `@Column(name = "\"value\"")` to generate properly quoted SQL

### Configuration Changes

#### application.properties, application-docker.properties
- Added `spring.sql.init.mode=always` (required in Spring Boot 3 for `schema.sql` execution; was implicit in Boot 2)

### Errors Encountered and Resolutions

| # | Error | Root Cause | Resolution |
|---|-------|------------|------------|
| 1 | Docker build DNS failure | Container couldn't resolve `archive.ubuntu.com` | Used `--network=host` flag for docker build |
| 2 | Gradle wrapper download failure at runtime | Container couldn't resolve `services.gradle.org` | Switched to multi-stage Dockerfile (build during image creation) |
| 3 | H2 SQL syntax error on `tags` table | `value` became a reserved keyword in H2 2.x (bundled with Spring Boot 3) | Quoted `"value"` in schema.sql |
| 4 | Hibernate SQL error on Tag queries | Generated SQL used unquoted `value` column | Added escaped quotes in `@Column(name = "\"value\"")` annotation |
| 5 | GET /tags returned 401 | `requestMatchers(GET, "/tags/**")` doesn't match `/tags` in Spring Security 6 | Added both `/tags` and `/tags/**` patterns |
| 6 | All authenticated endpoints returned 401 | Spring Security 6's `AuthorizationFilter` doesn't invoke `AuthenticationManager` for unauthenticated tokens | Modified `JWTAuthenticationFilter` to explicitly use `AuthenticationManager.authenticate()` |
| 7 | JaCoCo report config error | `html.enabled` deprecated in Gradle 8.x | Changed to `html.required = true` |

### Smoke Test Results

**40 passed, 0 failed out of 40 tests**

Tests cover:
- GET /tags (public)
- GET /articles (public, with filters: tag, author)
- POST /users (registration)
- POST /users/login (authentication)
- GET /user (authenticated)
- PUT /user (update profile)
- GET /profiles/{username} (public)
- POST /articles (create)
- GET /articles/{slug} (read)
- PUT /articles/{slug} (update)
- DELETE /articles/{slug} (delete, returns 204)
- POST /articles/{slug}/favorite (favorite)
- DELETE /articles/{slug}/favorite (unfavorite)
- POST /articles/{slug}/comments (create comment)
- GET /articles/{slug}/comments (list comments)
- GET /articles/feed (authenticated feed)
- POST /profiles/{username}/follow (follow user)
- DELETE /profiles/{username}/follow (unfollow user)
- Verify deleted article returns 404
