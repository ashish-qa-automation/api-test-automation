# Implementation Roadmap

**Project:** API Test Automation Framework  
**Status:** Phase 11 complete pending review. Phase 10 workflows are written; GitHub has not run them, so CI is not called green.

Work proceeds sequentially. Each phase ends with compile/test evidence, a completion report, a proposed commit, and an explicit wait for approval.

---

## Phase 0 — Repository discovery and architecture planning

**Objective:** Inspect the empty GitHub repository, freeze architecture, record technical decisions.

**Done in this phase:**

- Cloned `https://github.com/ashish-qa-automation/api-test-automation.git` into `D:\My\Projects\api-test-automation`.
- Confirmed `main` has a single initial commit, MIT license, and a stub README.
- Confirmed the Selenium UI framework is a separate directory and will not be modified.
- Produced this roadmap, `docs/ARCHITECTURE.md` and `docs/TECHNICAL_DECISIONS.md`.

**Exit criteria:** Architecture accepted. Open decisions in section “Approval gates” resolved or explicitly deferred.

**Not in this phase:** `pom.xml`, Java sources, CI, Cursor rules.

---

## Phase 1 — Framework foundation

**Objective:** A compiling Maven project with Java 17, dependencies, package skeleton, logging and a trivial TestNG test.

**Implement:**

- `pom.xml` with the versions recorded in `TECHNICAL_DECISIONS.md`.
- `maven-compiler-plugin` `--release 17`.
- `maven-surefire-plugin` wired to a placeholder TestNG suite.
- Package directories under `com.ashishqa.api` and `com.ashishqa.demo.booker`.
- Log4j2 test configuration.
- `.gitignore` for `target/`, Allure results, IDE files, `.env`.
- One framework smoke test that does not depend on an external API (for example, config loads or a local assertion) so `mvn test` can pass before RestClient exists.

**Validate:**

```text
mvn clean compile
mvn test
```

**Exit criteria:** Both commands succeed. No unused dependencies. License file untouched.

**Validation (2 Nov 2026):** `mvn -B clean compile` BUILD SUCCESS. `mvn -B test` BUILD SUCCESS — 4 tests, 0 failures. SLF4J bound to Log4j2. Java runtime during the run was 21; bytecode target is 17.

---

## Phase 2 — Configuration and environment management

**Objective:** Switch environments without code changes.

**Implement:**

- `FrameworkConfig` (immutable) and `ConfigLoader` with the priority order in the architecture doc.
- `framework.properties` plus `dev|qa|sit|uat` files with example URLs and empty secret placeholders.
- Support `-Denv=qa` and equivalent environment variables.
- Unit-level tests for override priority.

**Validate:** Loading `qa` vs `dev` yields different base URIs. System property overrides the file. No secrets committed.

**Validation (2 Nov 2026):** `mvn -B clean test` BUILD SUCCESS — 16 tests, 0 failures. `qa` loads Restful Booker; `-Denv=dev` loads `http://localhost:8080`; system properties beat environment variables; `toString()` redacts passwords.

---

## Phase 3 — Core API client architecture

**Objective:** Reusable HTTP communication for GET, POST, PUT, PATCH, DELETE.

**Implement:**

- `RequestSpecFactory` and `ResponseSpecFactory`.
- Instance-based `RestClient`.
- Support for headers, query params, path params and typed or JSON bodies.
- A thin application `BookingClient` skeleton that uses `RestClient` (no full business coverage yet).
- A health-check test against Restful Booker `GET /ping` to prove the client works.

**Validate:** `mvn test` hits `/ping` successfully, or fails with a clear connectivity error (not a timeout hang).

**Validation (2 Nov 2026):** `mvn -B clean test` BUILD SUCCESS — 17 tests, 0 failures. Live `GET https://restful-booker.herokuapp.com/ping` returned **201 Created**.

---

## Phase 4 — Authentication and security

**Objective:** Pluggable auth with no credential leakage.

**Implement:**

- `AuthStrategy` and the strategies listed in the architecture doc that are needed immediately: `NoAuth`, `BasicAuth`, `BearerTokenAuth`, `ApiKeyAuth`, `CookieTokenAuth`.
- Restful Booker `POST /auth` token provider with `ThreadLocal` cache.
- Redaction of token and password fields in logs.
- Negative test: write without token is rejected.

**Defer:** OAuth2 client-credentials and refresh tokens unless the demo API requires them.

**Validate:** Create/update/delete with cookie token succeeds. Logs and Allure attachments do not contain the raw token.

**Validation (2 Nov 2026):** `mvn -B clean test` BUILD SUCCESS — 24 tests, 0 failures. Live Restful Booker: create 200, unauthenticated DELETE 403, cookie-token PUT 200, cookie-token DELETE 201. Logs show `password` and `token` as `***`. Restful Booker returns **418** unless `Accept` is exactly `application/json`.

---

## Phase 5 — Models, serialization and test data

**Objective:** Typed payloads and JSON-driven tests.

**Implement:**

- Java 17 records for booking request/response bodies.
- Jackson `JsonCodec` using a single shared `ObjectMapper`.
- JSON test data for valid, invalid, missing-field and boundary cases.
- TestNG DataProviders that read those files.

**Validate:** Serialization round-trip test plus at least one parameterized API test.

**Validation (2 Nov 2026):** `mvn -B clean test` BUILD SUCCESS — 33 tests, 0 failures. Jackson round-trip of `Booking` records passed. Parameterized live tests: valid creates round-trip, boundary `totalprice=0` returns 200, missing `firstname` / `bookingdates` / empty body return **500** (Restful Booker's actual contract, not 400). Created records were deleted with a cookie token.

---

## Phase 6 — API validation and assertion architecture

**Objective:** Explicit, readable validation.

**Implement:**

- HTTP assertion helpers (status, headers, content-type, time).
- JSON Schema validation via `rest-assured` json-schema-validator.
- Error-response assertions for Restful Booker’s actual error shapes.
- Schema files for booking responses.

**Validate:** A passing schema test and a failing-on-purpose unit test of the helper are not required if the helper is thin; a real API test that uses schema validation is required.

**Validation (2 Nov 2026):** `mvn -B clean test` BUILD SUCCESS — 53 tests, 0 failures. Live create and GET matched booking JSON schemas. `GET /booking` matched the id-array schema. `POST /auth` token schema passed. Failed login returned **200** `{"reason":"Bad credentials"}`. Empty create **500** `Internal Server Error`. Missing booking **404** `Not Found`. Unauthenticated DELETE **403** `Forbidden`. Logs still redact `password` and `token`.

---

## Phase 7 — API test design and business workflows

**Objective:** A demonstration suite that looks like production API testing, not a REST Assured tutorial.

**Implement:**

- Positive, negative and boundary tests for booking endpoints.
- Auth happy path and unauthorized write.
- CRUD workflow with cross-endpoint assertions.
- Isolated data and cleanup (`delete` after create in an `@AfterMethod` or try/finally workflow).
- Document Restful Booker quirks (DELETE `201`, cookie auth, reset window) in the test notes / architecture, not by papering over them.

**Validate:** Smoke and a first regression class pass against the live API. Failures are classified (API down vs assertion vs framework).

**Validation (6 Nov 2026):** `mvn -B clean test` BUILD SUCCESS — 55 tests, 0 failures, including the CRUD workflow. `mvn -B test -DsuiteXmlFile=src/test/resources/suites/regression.xml` BUILD SUCCESS — 5 tests, 0 failures. Live journey: create 200, GET matched, PUT 200, PATCH partial body 200, DELETE **201 Created**, following GET **404**. Name filter returned the created id. Unauthenticated PUT and PATCH returned **403** and the booking was unchanged. Writes to an unknown id returned **405**. Negative price and inverted dates were stored. No transport failures, so no connection retry was added. Token and password stayed redacted. Maven profiles and `-Dgroups` remain Phase 8.

---

## Phase 8 — TestNG execution architecture

**Objective:** Suites, groups and parallel runs that actually work.

**Implement:**

- `smoke.xml`, `regression.xml`, `negative.xml`.
- Maven profiles `-Psmoke` / `-Pregression`.
- `-Dgroups=` support verified, not just documented.
- Parallel execution with thread-safe config and tokens.
- No retry-on-assertion-failure. Optional connection retry only if Phase 7 evidence shows Heroku cold-start noise, and only for transport failures.

**Validate:**

```text
mvn clean test
mvn clean test -Psmoke
mvn clean test -Pregression
mvn clean test -Dgroups=smoke
mvn clean test -Denv=qa
```

Each command is run before it is listed in README.

**Validation (6 Nov 2026):** All five commands BUILD SUCCESS, parallel methods, thread count 3. `mvn -B clean test`, `-Psmoke`, `-Dgroups=smoke` and `-Denv=qa` each ran **58** tests, 0 failures. `-Pregression` ran **5**. `-Dgroups=negative` on the smoke suite ran **7**, so the group filter excludes methods outside that group. `-Pnegative` ran **9**. `-Pregression -Dgroups=smoke` ran **1** (the CRUD workflow) and skipped the regression-only methods. No retry analyzer and no connection retry. Config swaps are hidden from other threads, and token caches stay on the calling thread.

---

## Phase 9 — Logging, debugging and reporting

**Objective:** An engineer can diagnose a failure from logs and Allure without re-running blindly.

**Implement:**

- Structured request/response logging with correlation ids.
- Allure environment file (env name, base URI, Java, OS).
- HTTP attachments via `AllureRestAssured`.
- Failure listener that records expected vs actual when available.

**Validate:** Generate Allure results from a Maven run and inspect that tokens are redacted.

**Validation (6 Nov 2026):** `mvn -B clean test` BUILD SUCCESS — 66 tests, 0 failures. `target/allure-results/environment.properties` records Environment=qa, the Restful Booker base URI, Java 21.0.2, Windows 11, and suite Smoke. Attachments show `X-Correlation-Id` on each call, `Cookie: token=***`, and auth bodies as `{"token":"***"}` and `"password":"***"`. A search of the results found no `password123` and no raw token value. Logs include `correlationId=` on the request line.

---

## Phase 10 — CI/CD integration

**Objective:** GitHub Actions runs smoke on PRs without developer laptops.

**Implement:**

- Workflow using Temurin 17.
- PR smoke, manual/scheduled regression.
- Upload Surefire and Allure results artifacts.
- No local paths, no unverified “CI is green” claim.

**Validate:** A real GitHub Actions run. Badge is added only after that run succeeds.

**Progress (6 Nov 2026):** `.github/workflows/ci.yml` and `.github/workflows/regression.yml` are written. Smoke runs on pull requests and pushes to `main`. Regression runs on demand and weekly, Monday 06:15 UTC. Both upload Surefire and Allure results even when Maven fails. No badge. `main` is ahead of `origin/main`, so GitHub has not run these workflows yet.

---

## Phase 11 — Cursor AI engineering rules

**Objective:** Project rules that describe this codebase, not generic Java advice.

**Implement:** `.cursor/rules/*.mdc` and `.cursor/README.md` after the architecture exists in code. Rules will tell the agent to reuse `RestClient`, keep assertions in tests, protect credentials, and run Maven.

**Validate:** Rules mention real package names and real commands from this repo.

**Validation (6 Nov 2026):** `.cursor/rules/framework.mdc`, `.cursor/rules/secrets.mdc`, and `.cursor/rules/api-tests.mdc` name `RestClient`, `AuthStrategy`, `SensitiveDataRedactor`, `RedactingAllureFilter`, `HttpAssert`, `BookingClient`, and `BaseApiTest`. They include `-Denv=qa` and the Maven commands that passed in Phase 8. `.cursor/README.md` indexes them. They forbid a CI badge until GitHub Actions has actually passed.

---

## Phase 12 — Documentation and open-source readiness

**Objective:** A stranger can clone, configure, run and understand the framework.

**Implement:** Rewrite `README.md`. Add `EXECUTION_GUIDE.md`, `CONFIGURATION_GUIDE.md`, `CONTRIBUTING.md`. Update architecture docs to match what was actually built. Keep MIT license.

**Validate:** README commands are a subset of commands that have been executed successfully. No feature is listed that was not implemented.

---

## Suggested commit points

One commit per approved phase on `main`, unless a development branch is requested. Commits are prepared and shown; they are not pushed until explicitly approved.

| Phase | Proposed message theme |
|-------|------------------------|
| 0 | Document architecture, roadmap and technical decisions |
| 1 | Establish Java 17 Maven foundation |
| 2 | Add environment configuration with override priority |
| 3 | Introduce RestClient and request specifications |
| 4 | Add pluggable authentication and token handling |
| 5 | Add models, Jackson codec and JSON test data |
| 6 | Add HTTP assertions and JSON schema validation |
| 7 | Add Restful Booker tests and chained workflows |
| 8 | Add TestNG suites, groups and parallel execution |
| 9 | Add redacted logging and Allure reporting |
| 10 | Add GitHub Actions smoke and artifact upload |
| 11 | Add project-specific Cursor rules |
| 12 | Complete open-source documentation |

---

## Approval gates

Before Phase 1 starts, the following decisions should be confirmed (defaults are the recommendations in `TECHNICAL_DECISIONS.md`):

1. Demonstration API: Restful Booker (recommended) or DummyJSON / other.
2. REST Assured line: 6.2.0 (recommended) or 5.5.x.
3. Allure line: 2.35.5 (recommended) or 3.0.0.
4. Module layout: single Maven module (recommended) or multi-module.
5. Git: continue on `main` (as currently instructed) and commit Phase 0 docs after review.

---

## Out of scope for the first delivery

- Publishing the core as a Maven Central library.
- GraphQL, gRPC, SOAP, WebSocket.
- Performance / load testing.
- Full OAuth2 authorization-code and refresh-token flows.
- WireMock / Testcontainers (candidate follow-up if public API flake blocks CI).
- Retrying failed assertions.
- Reusing or depending on the Selenium UI framework.
