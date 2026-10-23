# Topic 1 — Spring Core

Status: **closed** (2026-10-22/24). Hands-on proof built in `OrderService`/`OrderRunner`/`Order`/`OrderRepository`.

## Dependency Injection
**What**: instead of a class creating its own dependencies with `new`, it declares
what it needs (usually via constructor parameter) and something external — the
Spring IoC container — supplies a concrete instance.

**Why**: without it, a class hardcodes a concrete dependency (`new PaymentServiceImpl()`),
which means (1) you can't swap implementations without editing the class itself,
and (2) you can't unit-test the class in isolation — it always drags in the real
dependency and whatever that depends on. DI inverts this: control over *which*
concrete implementation gets used moves out of the class, into the container/config
— hence "Inversion of Control."

**Correction from first attempt**: beans are created at **runtime, during
application startup** — not at compile time. Compile time is just `javac`
producing `.class` files; nothing is running yet, no objects exist.

## Stereotype annotations — `@Component`/`@Service`/`@Repository`/`@Controller`
All four are functionally almost identical — `@Service`, `@Repository`, and
`@Controller` are themselves annotated with `@Component`, so Spring treats all
four the same way for bean scanning/registration. They exist mostly for
semantic clarity, with **one real behavioral difference**:
- `@Repository` enables automatic exception translation — JDBC/Hibernate
  exceptions get wrapped into Spring's `DataAccessException` hierarchy.
- `@Controller`/`@RestController` get special handling by `DispatcherServlet`
  for request mapping.
- `@Service` has zero extra behavior — pure documentation of intent.

## Bean scopes
- **`singleton`** (default) — one shared instance for the entire application
  context.
- **`prototype`** — a new instance every time the bean is requested.
- (web-app scopes exist too: `request`, `session`, `application` — know they
  exist, not needed in depth at this level.)

## Spring proxies — the core mechanism of Topic 1

**What**: Spring doesn't hand out your raw object when a bean has AOP-based
annotations (`@Transactional`, `@Cacheable`, `@Async`, `@Retryable`) — it wraps
it in a generated **proxy** object that sits between the caller and your real
instance.

**Why**: `@Transactional` has to run actual code — "start transaction → invoke
method → commit or rollback" — around your method. Spring can't inject that
into your already-compiled method body, so a proxy (code that runs
before/after the real call) is the mechanism used instead.

**Who**: any bean using AOP-based annotations. Plain beans with no such
annotations are never proxied.

**When**: the proxy only intercepts calls arriving **from outside the object**,
through the reference Spring itself handed out. It does NOT intercept a method
calling another method on `this`, from inside the same class — that's a
direct, unproxied Java call.

**Where this bites you**: any class with an internal helper method also marked
`@Transactional`/`@Cacheable`, called from another method in the same class.
Compiles fine, looks correct, silently does nothing. One of the most common
real-world Spring bugs and a very common interview question.

**Mechanism**: JDK dynamic proxy (when the class implements an interface) or
CGLIB proxy (generates a runtime subclass, used when there's no interface).

### The self-invocation bug — proven hands-on
Built: `OrderService.placeOrder()` (no `@Transactional`) calls
`this.saveOrder()` (has `@Transactional`, saves an `Order` entity, then
throws). Result: **order count after the failure was 1, not 0** — the insert
was never rolled back.

**Why, precisely, step by step**:
1. `OrderRunner` calls `orderService.placeOrder()` — goes through the real
   proxy, since `orderService` itself is a proxy reference.
2. Inside `placeOrder()`, `this.saveOrder()` — `this` is always the raw,
   real object, never the proxy, once you're executing code inside a method.
3. The call never passes through `TransactionInterceptor` — `@Transactional`
   on `saveOrder()` is read by nobody, enforced by nothing, for this call.
4. The insert executes as an ordinary, un-transacted write — no transaction
   context exists for a rollback to apply to.
5. Exception propagates, caught in the runner. Nothing undoes the insert.

### 3-line interview answer (memorize)
- **Problem**: `@Transactional` has no effect when called via `this.` from
  another method in the same class.
- **Cause**: `this` always refers to the raw bean, never the Spring-generated
  proxy; AOP interceptors only run on calls arriving through the proxy from
  an external caller.
- **Fix**: split into separate beans, or self-inject and call through the
  injected proxy reference (`@Autowired private OrderService self;` then
  `self.saveOrder()`), or `AopContext.currentProxy()` with `exposeProxy=true`.

### Gotcha hit while building this (unrelated to the core lesson, but real)
`Order` as an entity class name collided with `ORDER` being a reserved SQL
keyword — Hibernate's default unquoted table naming generated
`select count(*) from order ...`, which H2 rejected as a syntax error at the
word `order`. Fixed with `@Table(name = "orders")`. Common trap for domain
names that are also SQL keywords (`Order`, `User`, `Group`, `Table`).

## Mock question asked
*"We added `@Transactional` to a method, but when it throws an exception,
nothing rolls back — the half-written data is still in the database. Walk me
through how you'd debug this."*
(Answer: the 3-line summary above, expanded with "I'd check if the method is
called externally or via `this.` from within the same class first — that's
the most common cause.")
