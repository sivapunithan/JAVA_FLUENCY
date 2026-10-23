# Topic 2 — Spring Boot

Status: **in progress** (started 2026-10-24). Auto-configuration, starters,
and configuration binding covered; Q4 (request lifecycle) still open.

## Auto-configuration

**What**: a mechanism that automatically registers Spring beans based on
what's on the classpath and what you haven't already configured yourself —
no manual `@Configuration`/`@Bean` needed.

**Why**: without it, every project would need to manually wire up
`DataSource`, `EntityManagerFactory`, `TransactionManager`, embedded Tomcat,
Jackson's `ObjectMapper`, etc. — boilerplate nearly identical across every
project.

**Who**: triggered per-dependency — adding `spring-boot-starter-data-jpa`
triggers JPA auto-configuration classes, `spring-boot-starter-security`
triggers Security's, independently and composably.

**When**: at application startup, before your own beans are fully processed.

**Where this bites you**: "I added a dependency and app behavior changed with
zero code" — this is exactly what happened with H2 (it auto-connected to an
in-memory DB with zero config, purely because the `h2` dependency was on the
classpath).

**How it actually works**: each auto-configuration is a `@Configuration`
class guarded by conditional annotations:

```java
@Configuration
@ConditionalOnClass(DataSource.class)
@ConditionalOnMissingBean(DataSource.class)
public class DataSourceAutoConfiguration {
    @Bean
    public DataSource dataSource() { ... }
}
```

- `@ConditionalOnClass(DataSource.class)` — only activates if the relevant
  class is actually on the classpath (this is *why* adding H2 triggered a
  `DataSource` bean — before that, the class didn't exist on the classpath).
- `@ConditionalOnMissingBean(DataSource.class)` — only activates if you
  haven't already defined your own bean of that type; your own bean always
  wins, no conflict.

Spring Boot ships hundreds of these, one per supported library, each
evaluated at startup against your actual classpath + existing beans.

## Starters

**What**: a dependency like `spring-boot-starter-data-jpa` isn't code of its
own — it's a curated bundle of *other* dependencies (Hibernate core, Spring
Data JPA, HikariCP, Spring ORM, AspectJ) at pre-aligned compatible versions.

**The real mechanism**: a starter doesn't configure anything directly — it
just makes certain classes land on the classpath, which is what *flips the
`@ConditionalOnClass` switches* for the matching auto-configuration classes.
Adding a starter = turning on a set of conditional configuration classes.

**Noted correctly on first pass**: JPA = Jakarta Persistence API (renamed
from `javax.persistence` after the Jakarta EE transition), used for ORM.

## `application.properties`/`.yml` vs `@ConfigurationProperties`

**The file** (`application.properties`/`.yml`) is just a flat key-value
store — by itself it does nothing; something has to read the values.

**`@ConfigurationProperties`** is the mechanism that reads a whole *group* of
related keys and binds them into one strongly-typed Java object, instead of
pulling individual values one at a time with scattered `@Value("${...}")`
calls across many classes.

```java
@ConfigurationProperties(prefix = "app.ratelimit")
public class RateLimitProperties {
    private int maxRequests;
    private int windowSeconds;
    // getters/setters
}
```
```yaml
app:
  ratelimit:
    max-requests: 100
    window-seconds: 60
```
Spring Boot maps `app.ratelimit.max-requests` → `maxRequests` automatically,
type-checked at startup (wrong type = fails fast at boot, not buried in some
later runtime path).

**Practical difference**: `@Value` scattered across five classes is fragile
and hard to find; one `@ConfigurationProperties` class centralizes a related
settings group into a single, type-safe, testable object. Directly relevant
to the Week 2 AI-backend project's rate-limiting config.

## Q4 — request lifecycle (answered 2026-10-24)

**The one-sentence version — this is the actual interview answer, memorize
this, not component names:**
> One request comes in. One central thing receives it, figures out which of
> your methods should handle it, pulls out the data that method needs (path
> variables, query params, JSON body), calls your method, then converts
> whatever your method returns back into JSON for the response.

That sentence alone is a complete, correct, 2-3 YOE-level answer. No
component names required.

**Optional extra-credit layer** (only if an interviewer pushes deeper —
not something to memorize, just know it exists and what each piece's *job*
is, in plain words):
- The central receiver is called `DispatcherServlet` — one instance handles
  every request in the whole app (the "front controller" pattern).
- Something figures out *which* method to call, based on the URL + HTTP verb.
- Something else actually calls that method, and fills in its parameters
  (`@PathVariable`, `@RequestParam`, `@RequestBody`).
- Something converts JSON ↔ Java objects, both directions — incoming request
  body into your method's parameter object, and your method's return value
  back into the JSON response.
- All three of the above are pluggable/replaceable, which is why Spring MVC
  can be extended with custom format support without touching the central
  receiver.

Named versions if ever needed: `HandlerMapping` (which method), `HandlerAdapter`
(calls it, resolves its arguments), `HttpMessageConverter` (JSON ↔ Java, e.g.
Jackson). These names are not worth memorizing on their own — the *job* each
one does matters, the label doesn't.

## Hands-on (assigned, not yet confirmed done)
Add `app.demo.name`/`app.demo.retries` to `application.yaml`, write a
`@ConfigurationProperties(prefix = "app.demo")` class, inject it, print both
values at startup to confirm binding.

## Still open
- **Q4**: walk through what happens, end to end, when an HTTP request hits a
  `@GetMapping` endpoint — not yet answered.
- REST basics (`@RestController`, `@PostMapping`, `@RequestBody`/`@PathVariable`)
  — not yet covered.
- Centralized exception handling (`@ControllerAdvice`/`@ExceptionHandler`)
  — not yet covered.
- Actuator — not yet covered.

## Why this topic, in plain terms (per Punithan's question, 2026-10-24)
This isn't trivia — it's the same failure mode as the real interview's
locking question: being asked "why does this happen" under a framework that
makes things "just work," and not having the mechanism ready. Anyone can
generate a working Spring Boot app with AI tools now; what differentiates a
2-3 YOE candidate is whether they can explain *why* it behaves the way it
does when they didn't write the configuration themselves.
