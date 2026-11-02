# Technical Decisions

**Project:** API Test Automation Framework  
**Date:** 2 November 2026  
**Status:** Proposed in Phase 0. Versions will be re-checked against Maven Central when Phase 1 writes `pom.xml`.

This file records choices and the alternatives that were rejected. It is the place to disagree before code is written.

---

## 1. Repository and product identity

| Decision | Choice |
|----------|--------|
| Git repository | Existing `api-test-automation` on GitHub. Clone only; do not `git init` again. |
| Default branch | `main` until a development branch is explicitly approved. |
| Product name | API Test Automation Framework. The name TestForge is not used. |
| Java package | `com.ashishqa.api` (demo application: `com.ashishqa.demo.booker`) |
| Maven coordinates | `com.ashishqa:api-test-automation:1.0.0-SNAPSHOT` |
| License | Keep the existing MIT license and copyright holder. Do not add a second license file. |
| Sibling Selenium repo | Out of bounds. Patterns may inform design; classes must not be copied. |

---

## 2. Language and build

| Topic | Decision | Rationale |
|-------|----------|-----------|
| Language | Java 17 LTS (`--release 17`) | Requested baseline. Records, sealed types if needed, long support. |
| Build | Apache Maven 3.9.x | Already installed locally (3.9.6). Standard for Java TestNG projects. |
| Compiler plugin | `maven-compiler-plugin` 3.13.0 | Supports `--release 17`. |
| Surefire | `maven-surefire-plugin` 3.5.2 | TestNG 7 compatible; parallel and suite XML support. |
| Encoding | UTF-8 | Unambiguous on Windows and GitHub-hosted runners. |
| Lombok | **Not used** | Records cover immutable DTOs. Lombok would hide Java from readers of a public SDET repo. |

### Local JDK mismatch (risk)

| Runtime | Version observed 2 Nov 2026 |
|---------|------------------------------|
| `java` on PATH | OpenJDK 11.0.31 (Microsoft) |
| Maven (`mvn -version`) | JDK 21.0.2 (Oracle) |
| Project target | Java 17 |

Maven can compile `--release 17` using JDK 21. GitHub Actions will pin **Temurin 17**. Developers on JDK 11 cannot build this project; that is acceptable and will be documented. PATH Java 11 is irrelevant as long as Maven uses 17+.

---

## 3. Baseline dependency versions

These are the intended first pins. Phase 1 will confirm they resolve together. Latest-at-any-cost versions were avoided.

| Component | Proposed version | Why this and not newer/older |
|-----------|------------------|------------------------------|
| REST Assured | **6.2.0** | 6.2 fixed parallel races in static configuration and dropped the Groovy implementation of the core. 6.1 added modern JSON Schema drafts. 6.3.0 (23 Oct 2026) is a 7.0 deprecation preview with far fewer adopters — too new for a foundation pin. 5.5.x is the conservative fallback if 6.2 conflicts with Allure or Jackson. |
| json-schema-validator | **6.2.0** | Same release line as REST Assured. |
| TestNG | **7.11.0** | Java 11+ bytecode, stable. 7.12.0 (Jan 2026) is usable but Allure 2.x examples still commonly cite 7.10/7.11. 7.11 satisfies Allure 3’s “TestNG 7.10+” requirement if we later upgrade Allure. |
| Allure TestNG + REST Assured adapter | **2.35.5** | Latest 2.x as of Sep 2026. 2.34.0+ supports REST Assured 6. Allure **3.0.0** is a major line with few Maven usages yet; keep as a conscious upgrade, not the first pin. |
| Jackson | **2.18.2** (verify patch on Central) | 2.18 is LTS through end of 2026. Align REST Assured’s Jackson via `dependencyManagement` if versions clash. |
| SLF4J API | **2.0.17** | TestNG 7.x already depends on SLF4J 2. Main-scope logging needs the API on the compile classpath. |
| Log4j2 | **2.25.5** | 2.25 is the LTS/maintenance line. 2.24 is end-of-maintenance. 2.26.1 is active development. 3.x is still beta. Use `log4j-slf4j2-impl`, not `log4j-slf4j-impl`. |
| Allure Maven plugin | **2.15.2** (verify) | Report generation from `target/allure-results`. Adapter version and CLI/plugin version are not the same number space; they do not need to match. |

REST Assured must be declared **before** TestNG in `pom.xml` so Hamcrest versions resolve the REST Assured way (upstream recommendation).

Phase 1 confirmed these pins resolve from Maven Central and compile on JDK 21 targeting `--release 17`. REST Assured 6.2.0 also pulls a Jackson 3 BOM (`tools.jackson:jackson-bom:3.2.3`) for its own internals. Application DTOs still use Jackson 2.18.2 via `jackson-databind`. Phase 5 will confirm the two Jackson lines do not collide when we serialize booking records. If they do, the fallback is to let REST Assured own JSON conversion and drop the explicit Jackson 2 pin.

Dependencies **not** added in the first delivery: AssertJ, Cucumber, ExtentReports, dotenv-java, WireMock, Testcontainers, Spring, Guice, Apache POI, owner, lombok.

---

## 4. Module layout

**Decision:** one Maven module.

**Rejected:** `api-test-automation-core` + `api-test-automation-demo` multi-module.

A second module is justified when the core is published or consumed by multiple repos. This repository is the core and the demonstration in one public project. Multi-module would add reactor complexity without a consumer.

Package separation (`com.ashishqa.api` vs `com.ashishqa.demo.booker`) gives the same conceptual boundary.

---

## 5. HTTP client design

**Decision:** instance `RestClient` built from `RequestSpecFactory`, returning REST Assured `Response` (or a thin immutable wrapper only if we need extra metadata such as correlation id).

**Rejected:**

| Alternative | Why not |
|-------------|---------|
| Static `RestAssured.given()` in every test | Duplicates base URI, headers, filters; unsafe if tests mutate globals under parallel load |
| One `ApiUtils` with every verb and assertion | God class; hides failures |
| Retrofit / Apache HttpClient 5 as the primary client | The project’s purpose is REST Assured expertise |
| Wrapping every call in a custom `ApiResponse<T>` from day one | Extra type with no proven need. Add only if correlation id or deserialized body should travel together |

Application-specific clients (`BookingClient`) own paths and payloads. They do not reimplement HTTP.

---

## 6. Configuration format

**Decision:** Java `.properties` files plus system properties and environment variables.

**Rejected:** YAML (extra parser), JSON config (poor for comments), Typesafe Config, Spring profiles, dotenv as a required runtime.

Properties match the existing public UI framework’s operational model, need no extra library, and map cleanly to `-Dkey=value`.

Environment variable naming: uppercase the property key and replace `.` with `_`. Example: `api.baseUri` ← `API_BASE_URI`.

---

## 7. Demonstration API

**Decision (recommended):** Restful Booker at `https://restful-booker.herokuapp.com`.

Evaluated on 2 November 2026:

| Candidate | Auth | Writes persist | CRUD + chaining | CI risk | Verdict |
|-----------|------|----------------|-----------------|---------|---------|
| Restful Booker | Cookie token via `/auth` | Yes, until ~10 min reset | Yes | Heroku cold start; shared data | **Primary** |
| DummyJSON | JWT Bearer | Simulated; not stored | Weak for chaining | More stable, rate limits | Fallback / second demo later |
| GoRest | Personal Bearer token | Yes (per-token, daily reset) | Yes | Requires a stored secret | Avoid for a zero-setup public clone |
| JSONPlaceholder | None | Fake | No | Low | Too limited |
| Swagger Petstore | Variable | Shared | Yes | Host reliability | Common but noisy |
| Local WireMock | Ours | Ours | Ours | Lowest flake | Best CI isolation; weaker “real API” story. Follow-up if Heroku blocks Phase 10 |

Restful Booker `/ping` responded `Created` during Phase 0. That is the documented health check, not HTTP 200. Tests must expect `201` for ping and delete, not invent a cleaner contract.

Public demo credentials (`admin` / `password123`) are part of the vendor documentation. They may live in example config with a comment that they are public demo values. They are not GitHub Secrets.

---

## 8. Authentication scope

**Decision:** strategy interface now; implement NoAuth, Basic, Bearer, API key, Cookie token in Phase 4. Token fetch for Restful Booker is per-thread.

**Rejected now:** OAuth2 authorization code, refresh-token rotation, mutual TLS, SAML.

Cookie token is not a vanity strategy. Restful Booker rejects `Authorization: Bearer` on write endpoints and expects `Cookie: token=...`. Supporting only Bearer would make the demo API unusable for PUT/PATCH/DELETE.

---

## 9. Serialization and models

**Decision:** Jackson databind with Java 17 records for application DTOs. One `ObjectMapper` created by a small factory (thread-safe after configuration).

REST Assured can serialize objects itself; the codec exists so tests and DataProviders use the same mapper as the client (FAIL_ON_UNKNOWN_properties policy, date format for Restful Booker’s `YYYY-MM-DD`).

**Rejected:** Gson (second JSON stack), JSON-Java org.json as the primary model, putting DTOs in `src/main/java` for a demo-only API.

---

## 10. Assertions

**Decision:** TestNG `Assert` + REST Assured matchers + a small HTTP helper class. Business expectations remain in test methods or `demo.booker.assertions`.

**Rejected:** AssertJ (extra style), SoftAssert as the default (masks the first failure in API contract tests), moving all `then().body(...)` into `RestClient`.

Schema validation is opt-in per test via the json-schema-validator module, not a hidden filter that fails every response.

---

## 11. Parallelism and retries

**Decision:** Design for TestNG parallel methods/tests from Phase 3 (instance specs, ThreadLocal tokens, unique booking names). Turn parallel **on** in suite XML in Phase 8 after the suite is green serially.

**Retries:** none by default. If Heroku cold starts produce connection failures, a **transport-only** retry (connect timeout / `UnknownHostException`) may be added later, never on assertion failure or 4xx.

REST Assured 6.2 is part of this decision: 6.2 advertises that parallel tests no longer fail from races in static configuration. We still will not rely on mutable `RestAssured` static defaults.

---

## 12. Logging and reporting

**Decision:** SLF4J + Log4j2 for logs; Allure 2.35.5 for reports; `AllureRestAssured` as a filter on the request spec, not ad-hoc attachments in every test.

Redaction happens in one place (`SensitiveDataRedactor`) used by both the log filter and Allure configuration so the two channels cannot drift.

**Rejected:** ExtentReports (second report), Logback (Log4j2 was specified), printing full tokens at ERROR level “for debugging”.

---

## 13. CI/CD

**Decision:** GitHub Actions, Temurin 17, Maven wrapper **not** required initially because the runner can install Maven via `setup-java` cache. A wrapper can be added in Phase 10 if runner Maven versions become a problem.

Smoke on pull request. Regression on `workflow_dispatch` (and schedule only if the public API can tolerate it — Restful Booker should not be hit by a heavy nightly if that looks like abuse).

Do not add a green badge until a workflow run on `ashish-qa-automation/api-test-automation` has actually passed.

---

## 14. Cursor rules

**Decision:** write `.cursor/rules/` in Phase 11 from the implemented packages and commands, not from this document’s aspirations.

Generic “write clean Java” rules that do not mention `RestClient`, `AuthStrategy`, or `-Denv` are not acceptable.

---

## 15. Open decisions for approval

| ID | Question | Recommendation | Impact if changed |
|----|----------|----------------|-------------------|
| D1 | Demonstration API | Restful Booker | DummyJSON would drop true CRUD chaining and change auth to JWT |
| D2 | REST Assured line | 6.2.0 | 5.5.x forces Allure ≤ 2.33.x and reintroduces known parallel static-config risk |
| D3 | Allure line | 2.35.5 | 3.0.0 needs Java 17 (we have it) and TestNG 7.10+ (we have it) but is a young major |
| D4 | Maven modules | Single module | Multi-module delays Phase 1 without a second consumer |
| D5 | Work branch | `main` | A `develop` branch can be created if preferred before the first push |
| D6 | Commit Phase 0 docs | Yes, after approval, no push | Push requires a separate explicit approval |

---

## 16. Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Restful Booker downtime / Heroku sleep | Medium | Short connect and response timeouts; `/ping` gate; fail fast; optional later WireMock |
| Data reset during a workflow | Medium | Keep chained tests short; create data inside the workflow |
| Known API bugs fail “correct” tests | High | Assert documented actual behaviour; comment the spec mismatch |
| Jackson clash with REST Assured 6 | Low/Medium | `dependencyManagement` alignment in Phase 1 |
| Allure 2.35.5 + REST Assured 6.2 filter issues | Low | Fallback: Allure 2.34.0 or pin REST Assured 6.0.1 (most-used 6.x) |
| JDK 11 on PATH confuses contributors | Medium | README and CI pin 17; enforcer plugin in Phase 1 (`requireJavaVersion` 17+) |
| Public repo leaks tokens in Allure artifacts | Medium if we are careless | Redactor tests; never log raw `Cookie` / `Authorization` |

---

## 17. What Phase 1 will not do

Phase 1 establishes the build. It will not implement RestClient, auth, Allure attachments, CI, or Restful Booker scenarios. A green `mvn test` in Phase 1 means the skeleton and a local TestNG smoke test passed, not that API coverage exists.
