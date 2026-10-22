# Goals — JavaFluency

## Why this project exists
Interview prep reframed from generic DSA grinding to **Java fluency for AI-era
backend interviews**. The reasoning: AI coding tools make shallow syntax
knowledge worthless, so interviews now probe (a) Java fundamentals that
autocomplete can't fake, (b) whether you can spot bugs in AI-generated or
legacy code, (c) whether you've actually built the pattern companies now
expect — a backend service that calls an LLM safely (timeouts, retries,
caching, rate limiting).

This is kept **separate from DSA practice** (`~/DSA/Learning-Java`) — DSA is
managed independently there, not tracked or touched here.

## What we're building here
A single Spring Boot app (H2, Spring Data JPA, Spring Security, Spring Web —
already scaffolded via Spring Initializr) used as the hands-on ground for two
tracks:

1. **Core Java fundamentals, proven through code, not just read about.**
   Each gap gets an exercise that demonstrates the real mechanism — e.g. the
   `HashSet`/mutable-key bug already built, proving *why* lookups can silently
   fail; bounded generics via a `Student<T>` class and a `higherScorer`
   generic method, tying directly into why `JpaRepository<T, ID>` is shaped
   the way it is.
2. **A real AI-integration backend feature** inside this same Spring Boot app
   — an endpoint that calls an LLM API with timeout, retry, caching (H2-backed
   or in-memory), and rate limiting. The deliverable interviewers now
   explicitly ask "have you built this."

## Ground rules for how we work
- **Guided, with selective write access (updated 2026-10-22).** Default is
  still Claude explains concepts, asks questions, points at what to try next,
  and does not edit files — Punithan writes exercises himself when he's
  learning something new, to break and learn it hands-on. But Punithan has
  explicitly authorized Claude to write files directly for concepts he
  already understands (e.g. the `Order` entity/repository scaffolding, once
  he confirmed he already knows why entities exist) — this saves time on
  boilerplate he's not trying to learn from typing out. When in doubt, default
  back to guided/chat-only and ask.
- **5W framing before code (added 2026-10-22, non-negotiable).** Every
  mechanism gets What/Why/Who/When/Where explained before any exercise or
  code — see [INTERVIEW_PREP.md](INTERVIEW_PREP.md) for the full loop.
- **Hands-on over toy demos.** Prefer exercises that prove a real mechanism
  (bucket collisions, mutable-key bugs, type-inference conflicts) over
  abstract explanations alone — verbal understanding gets checked, then
  backed by running code.
- Progress tracked loosely here and in conversation — no rigid day-by-day
  schedule being enforced; pace follows what's actually being built.

## Current state (as of 2026-10-14)
- Spring Boot project scaffolded (Web, Data JPA, Security, H2).
- Core Java exercises done so far (built in the prior `~/Java-Fluency` plain-Maven
  setup, concepts carry over): HashMap/HashSet internals + mutable-key bug,
  generics (`Student<T>`, bounded type parameters, generic methods, wildcard
  vs generic-method type inference).
- **Plan temporarily superseded**: an interview landed for next week, so the
  open-ended pace described above is on hold. See
  [INTERVIEW_PREP.md](INTERVIEW_PREP.md) for the active, compressed,
  topic-ordered plan driving day-to-day work right now. Once that interview is
  done, fold back into the slower fundamentals-depth track here (the JPA
  entity/repository work, Spring Security, and the AI-integration endpoint
  below were queued up before the interview came in, and feed directly into
  INTERVIEW_PREP.md's Topic 3 and Topic 7 anyway, so nothing here is wasted).

## Longer-term next step (post-interview, or wherever INTERVIEW_PREP.md creates
the opening)
Build the first `@Entity` + `JpaRepository<T, ID>` pair, tying directly back
into the generics already learned — `JpaRepository<User, Long>` is the same
generic-interface pattern as the `Student<T>`/`higherScorer` exercises, just
written by the Spring team instead of by hand. This is also exactly the
hands-on exercise Topic 3 (JPA/Hibernate) in INTERVIEW_PREP.md will use.
