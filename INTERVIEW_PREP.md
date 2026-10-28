# Interview Prep Plan — 2-3 YOE Spring Boot Interview (next week)

**This supersedes the open-ended pace in [GOALS.md](GOALS.md) until the
interview is done.** GOALS.md still holds the overall project purpose and
ground rules; this file is the active, detailed, day-to-day plan right now.
Target depth throughout: 2-3 years of experience — skip deep internals only
senior roles get asked (detailed GC tuning, advanced sharding, JVM internals).

## Why this order, and why it matters in the AI era

Interviewers now assume boilerplate comes from a tool, so they probe what
actually separates someone who understands the mechanism from someone who's
only ever pasted working code. Every topic below is picked because it's
exactly where an AI-assisted candidate's understanding tends to be shallow,
and where interviewers have specifically adjusted questions to probe for that.

1. **Spring Core** — everything else (JPA's `@Transactional`, Security's
   filter chain, `@RestController`) sits on top of the IoC container and the
   proxy mechanism. Can't reason about why `@Transactional` silently
   no-ops on self-invocation without proxies first.
2. **Spring Boot** — the layer most people use without knowing what's
   underneath. "Why did adding a dependency change behavior with zero code?"
   is a live AI-era question, since auto-config is exactly the kind of thing
   you can use for years without understanding.
3. **JPA/Hibernate + `@Transactional`** — N+1 queries, lazy loading, and
   propagation/isolation are the single most common gotcha category at this
   level, and the most common place AI-generated code silently does something
   expensive that looks correct.
4. **Core Java** — already underway. The "can you explain what autocomplete
   wrote" layer.
5. **Multithreading** — directly relevant to calling external APIs (including
   LLMs) without blocking; ties back into the AI-backend-project motivation
   in GOALS.md.
6. **SQL + DB** — the layer under JPA. Real exposure already exists here
   (RBL Bank GRN/AP platform, Wells Fargo locking work) — this is about
   surfacing that into clean interview answers, not learning from scratch.
7. **Resume projects + system design walkthrough** — this is the shape of
   question companies now ask to probe the AI-era backend pattern (safe
   external calls, idempotent retries, cost/rate control), even when the
   example given is a wallet instead of an LLM call.

## The loop, per topic/level
1. 3-4 questions at the current level, asked *before* any explanation.
2. Blunt grading — right / wrong / missing — then the correct mechanism,
   explained with real technical depth, no analogies at this stage.
3. **Language/framework-agnostic framing first (added 2026-10-28, per
   Punithan's explicit request).** Before the Spring-specific explanation,
   state the general backend concept that's true regardless of language or
   framework — e.g. "intercept a call to run code before/after it" (not
   "Spring AOP proxy"), "one entry point routes to a handler and serializes
   the response" (not "DispatcherServlet"). Spring's specific annotations and
   class names are then introduced as *one implementation* of that general
   concept, so the knowledge transfers if Punithan ever works in a different
   stack (Node/Express, Python/FastAPI, Go). This is on top of, not instead
   of, the 5W framing and the one-sentence-first calibration below.
4. **Every mechanism explained gets framed with the 5W before any code**:
   - **What** it actually is.
   - **Why** it exists / what problem it solves.
   - **Who** it applies to (which kind of class/annotation/situation).
   - **When** it triggers, or is the right tool to reach for.
   - **Where** it shows up as a real bug/failure in practice.
   This is non-negotiable per Punithan's explicit feedback (2026-10-22) — do
   not jump straight to code/exercises without this framing first.
5. One small hands-on exercise in this project, run and observed — where
   relevant, break something on purpose (N+1, mutable HashMap key), then fix it.
6. A 3-line "problem → cause → fix" summary — this is the memorized interview answer.
7. One rapid mock-interview-style question, judged on a spoken-style answer.

**Memorization calibration (added 2026-10-24, per Punithan's explicit
feedback — he can't retain a pile of component names after a day, and
doesn't need to).** Every mechanism gets a **one-sentence, jargon-free
version first** — that sentence alone must be a complete, correct 2-3 YOE
interview answer on its own. Exact internal component/class names
(`HandlerMapping`, `HandlerAdapter`, etc) go in a clearly separate "optional
extra credit" section — useful only if an interviewer pushes deeper, never
something Punithan is expected to recall unprompted. What matters is
explaining *what something does, in his own words* — not reciting Spring's
internal class names.

Say "next" to advance, "mock" for a rapid-fire review of everything covered so far.

---

## Topic 1 — Spring Core

**Level 1 (basic)**: IoC container concept, what a bean is, `@Component`
family (`@Component`/`@Service`/`@Repository`/`@Controller`) and why four
near-identical annotations exist, constructor vs field vs setter injection
and why constructor injection is preferred, bean scopes (`singleton`,
`prototype`, request/session scopes) and the default.

**Level 2 (intermediate)**: bean lifecycle (`@PostConstruct`/`@PreDestroy`,
`InitializingBean`/`DisposableBean`), circular dependency errors and how to
actually fix them (not just `@Lazy` as a band-aid), `@Qualifier` vs
`@Primary`, `@Value` and property injection, what `ApplicationContext` vs
`BeanFactory` actually differ on.

**Level 3 (advanced, 2-3 YOE ceiling)**: how Spring proxies work (JDK dynamic
proxies vs CGLIB, when each is used), why `@Transactional`/`@Cacheable`
silently don't apply on self-invocation (calling a `@Transactional` method
from another method in the *same* class bypasses the proxy entirely) and the
fix (self-injection, `AopContext.currentProxy()`, or restructuring), bean
post-processors at a conceptual level (what `BeanPostProcessor` is *for*, not
how to write a custom container).

## Topic 2 — Spring Boot

**Level 1**: what auto-configuration actually does, what a "starter" is and
why `spring-boot-starter-data-jpa` pulling in Hibernate+HikariCP+Spring Data
is not magic, `application.properties`/`.yml` basics and profiles
(`application-dev.yml` etc), building a basic REST endpoint
(`@RestController`, `@GetMapping`/`@PostMapping`, `@RequestBody`/`@PathVariable`).

**Level 2**: `@ConditionalOnClass`/`@ConditionalOnMissingBean` — how
auto-config decides what to wire in, overriding auto-configured beans,
`@ConfigurationProperties` vs scattered `@Value`, centralized exception
handling (`@ControllerAdvice` + `@ExceptionHandler`), validation
(`@Valid`, `@NotNull` etc on request DTOs) and where the error response
actually gets built.

**Level 3**: Actuator — what `/actuator/health` and `/actuator/metrics`
actually expose and why that matters for production debugging, how to read
Boot's own auto-configuration report (`--debug` flag) to actually diagnose
"why didn't my bean get created," versioning REST APIs, idempotency keys on
POST endpoints (direct link to the Topic 7 wallet system design).

## Topic 3 — JPA/Hibernate + `@Transactional`

**Level 1**: `@Entity`/`@Id`/`@GeneratedValue` basics, `@OneToMany`/
`@ManyToOne`/`@ManyToMany` and `mappedBy`, what `JpaRepository<T, ID>`
gives you for free, the difference between `save()` triggering INSERT vs
UPDATE.

**Level 2**: lazy vs eager loading and `LazyInitializationException`
(what actually throws it — accessing a lazy association outside an open
session/transaction), the N+1 query problem — what causes it, how to spot
it (enable SQL logging, count queries), fixes (`JOIN FETCH`, `@EntityGraph`,
batch fetching), first-level (persistence context) vs second-level cache.

**Level 3**: `@Transactional` propagation types (`REQUIRED`, `REQUIRES_NEW`,
`NESTED` — know these three cold, the rest are rare in interviews), isolation
levels at a conceptual level (READ_COMMITTED vs REPEATABLE_READ, what a
phantom read is), optimistic locking with `@Version` vs pessimistic locking
with `@Lock(PESSIMISTIC_WRITE)` — direct tie-in to the Wells Fargo
optimistic-locking resume project, so this should be an easy win once the
mechanism is explained properly.

**Hands-on plan**: build a small `Author`/`Book` one-to-many, deliberately
trigger N+1 by fetching all authors and looping to print each one's books,
observe the query count in the console log, then fix it with `JOIN FETCH` or
`@EntityGraph` and confirm the query count drops.

## Topic 4 — Core Java

Continuing what's already in progress (see GOALS.md "Current state"):
collections internals (done: HashMap/HashSet + mutable-key bug), generics
(done: bounded types, generic methods, wildcards) — remaining before this
topic is "solid": OOP (why prefer composition over inheritance, interface
default methods), exceptions (checked vs unchecked, when to use which,
custom exception design), streams (map/filter/reduce/collect, when a stream
is the wrong choice for an interview whiteboard).

## Topic 5 — Multithreading

**Level 1**: `Thread` vs `Runnable` vs `Callable`, what `ExecutorService` is
and why raw `new Thread()` doesn't scale, `synchronized` keyword basics.

**Level 2**: `volatile` — what visibility problem it solves and what it does
*not* solve (it's not atomicity), `ExecutorService` thread pool types
(fixed, cached, scheduled) and when to pick which, `CompletableFuture`
basics (`supplyAsync`, `thenApply`, `thenCompose`).

**Level 3**: race conditions — concrete example and fix with `AtomicInteger`
vs `synchronized`, deadlock conditions and how to avoid them (lock ordering),
virtual threads (Java 21) vs platform threads and why virtual threads matter
specifically for high-concurrency blocking I/O (like calling an LLM API) —
direct tie-in to GOALS.md's AI-backend project.

## Topic 6 — SQL + DB

**Level 1**: JOIN types (INNER/LEFT/RIGHT/FULL), what an index actually is
and why it speeds up lookups, primary key vs unique constraint.

**Level 2**: composite indexes and column order mattering, `EXPLAIN`/query
plans at a conceptual level, N+1 from the SQL side (what the generated SQL
actually looks like, tying back to Topic 3), basic migration tooling
(Flyway/Liquibase — what problem they solve: versioned, repeatable schema
changes across environments).

**Level 3**: row-level locking (`SELECT ... FOR UPDATE`) vs optimistic
locking from the app layer, isolation levels from the DB side, sharding only
at a conceptual level (what problem it solves, not implementation detail) —
explicitly capped here per the "skip senior-only internals" instruction.

## Topic 7 — Resume projects + system design walkthrough

Prep for a clean, structured walkthrough of: the RBL Bank procurement/GRN/AP
platform, the Wells Fargo asset management + ServiceNow integration +
optimistic/pessimistic locking work, the Amagi Delegation of Authority
module, and the personal projects (event-driven streaming app with Kafka/API
Gateway, ticket-booking app with locking strategies).

Then a basic system design walkthrough using a **wallet app** as the running
example: caching (what to cache, cache invalidation triggers), idempotency
keys (preventing double-charges on retry — this is the same mechanism a
safe LLM-calling backend needs for retrying a partially-failed call),
rate limiting (token bucket at a conceptual level), and where a lock
(optimistic vs pessimistic) is actually needed vs where it's overkill.

---

## Ground rules
Same as [GOALS.md](GOALS.md): guided, not written-for-you. Claude doesn't
touch files in this project — code shown in chat is for Punithan to type in
himself. Tone is blunt, no cheerleading, wrong or shallow answers get called
out directly.

## Weak spots (updated as we go)
*(empty — populated after each topic's grading)*

## Progress
- **Topic 1 — Spring Core**: Level 1 in progress (4 questions asked, awaiting answers).
