# API Test Automation Framework — Architecture

**Project:** `api-test-automation`  
**Repository:** https://github.com/ashish-qa-automation/api-test-automation  
**License:** MIT (already present in the repository; preserve as-is)  
**Status:** Phase 0 — architecture only. No framework code has been implemented yet.

This document describes the intended architecture. It is not a claim that the components already exist.

---

## 1. Purpose

This project is a reusable REST API test automation framework. The goal is that a new REST application can be tested by adding:

1. Environment configuration.
2. Application-specific clients and models.
3. Tests, workflows and test data.

The core HTTP, configuration, authentication, logging and reporting infrastructure should not be redesigned for each new API.

This repository is a public, recruiter-ready demonstration of senior SDET engineering. It is not a copy of private client frameworks and it is independent of the existing Selenium UI framework in the same workspace.

---

## 2. Current repository state

Inspected on 2 November 2026 after cloning `origin/main`.

| Item | Finding |
|------|---------|
| Branch | `main`, tracking `origin/main` |
| Remote | `https://github.com/ashish-qa-automation/api-test-automation.git` |
| History | One commit: `f504c39` — *Initial commit* |
| Files | `LICENSE` (MIT, Copyright 2026 Ashish Kumar), `README.md` (GitHub default stub) |
| Java / Maven | Not present. No `pom.xml`, no source tree, no CI |
| Local JDK | Maven currently runs on JDK 21. PATH `java` is OpenJDK 11. Project target remains **Java 17** |

Existing useful work to preserve: the MIT license and git history. The README will be rewritten in Phase 12 once features are implemented and validated.

---

## 3. Design principles

1. **Separation of concerns.** HTTP communication, configuration, authentication, assertions and business workflows live in different packages.
2. **Single responsibility.** No god client, no catch-all `ApiUtils`.
3. **Reusable without ceremony.** Application teams add clients and tests; they do not fork the core.
4. **Configuration-driven execution.** Environment, timeouts, auth type and base URI change without source edits.
5. **Instance-based HTTP, not global static state.** REST Assured request specifications are built per client/thread. This is required for safe parallel execution.
6. **Assertions stay explicit.** Helpers may reduce duplication; they must not hide the expected business outcome.
7. **Credentials never enter git.** Example config files contain placeholders only.
8. **Every abstraction earns its place.** No OAuth refresh-token machinery, no multi-module Maven build, and no retry-on-assertion-failure unless a later phase proves they are needed.

---

## 4. Layered architecture

```text
┌─────────────────────────────────────────────────────────────┐
│  Tests / Workflows / DataProviders                          │
│  (application-specific scenarios and chained business flows)│
└────────────────────────────▲────────────────────────────────┘
                             │ explicit assertions
┌────────────────────────────┴────────────────────────────────┐
│  Application clients and models                             │
│  BookingClient, AuthClient, request/response records        │
└────────────────────────────▲────────────────────────────────┘
                             │ typed requests / responses
┌────────────────────────────┴────────────────────────────────┐
│  Framework core (src/main/java)                             │
│  Config → Auth strategies → Request/Response specs          │
│  RestClient → Serialization → Logging / redaction           │
└─────────────────────────────────────────────────────────────┘
                             │
                      REST Assured
                             │
                      Target REST API
```

HTTP communication never asserts business rules. Tests never construct raw REST Assured requests when an application client already exists for that endpoint.

---

## 5. Proposed package structure

Single Maven module. Framework code in `src/main/java`. Test infrastructure and the demonstration application in `src/test/java`.

```text
api-test-automation/
├── pom.xml
├── LICENSE                          ← already present; do not replace
├── README.md                        ← rewrite in Phase 12
├── .gitignore
├── docs/
│   ├── ARCHITECTURE.md              ← this file
│   ├── IMPLEMENTATION_ROADMAP.md
│   ├── TECHNICAL_DECISIONS.md
│   ├── EXECUTION_GUIDE.md           ← Phase 12
│   ├── CONFIGURATION_GUIDE.md       ← Phase 12
│   └── CONTRIBUTING.md              ← Phase 12
├── .cursor/                         ← Phase 11, based on implemented code
├── .github/workflows/               ← Phase 10
└── src/
    ├── main/java/com/ashishqa/api/
    │   ├── config/                  configuration loading and typed settings
    │   ├── authentication/          AuthStrategy implementations
    │   ├── specifications/          request and response specifications
    │   ├── clients/                 method-agnostic RestClient
    │   ├── serialization/           Jackson mapper and JSON codec
    │   ├── logging/                 redacting request/response filters
    │   ├── reporting/               Allure helpers used by the core
    │   └── utilities/               small focused helpers (file, schema path)
    ├── main/resources/
    │   └── log4j2.xml               default logging if needed on main classpath
    └── test/
        ├── java/com/ashishqa/api/
        │   ├── base/                BaseApiTest and suite lifecycle
        │   ├── listeners/           TestNG / Allure listeners
        │   ├── assertions/          reusable HTTP-level assertion helpers
        │   └── dataproviders/       JSON DataProvider utilities
        ├── java/com/ashishqa/demo/booker/
        │   ├── clients/             BookingClient, AuthClient
        │   ├── models/              request and response records
        │   ├── assertions/          booking-specific assertions
        │   ├── tests/               positive, negative, boundary tests
        │   └── workflows/           chained create → update → delete flows
        └── resources/
            ├── config/
            │   ├── framework.properties
            │   └── env/
            │       ├── dev.properties
            │       ├── qa.properties
            │       ├── sit.properties
            │       └── uat.properties
            ├── testdata/booker/
            ├── schemas/booker/
            ├── suites/
            │   ├── smoke.xml
            │   ├── regression.xml
            │   └── negative.xml
            ├── log4j2-test.xml
            └── allure.properties
```

Deviation from the original sketch: demonstration tests live under `com.ashishqa.demo.booker`, not under `com.ashishqa.api.tests`. That keeps the reusable framework visually and physically separate from one sample application, which is the point of the project.

`models/` in `src/main/java` is reserved for framework-level types only (for example a generic error envelope if one is actually needed). Application DTOs stay with the application package.

---

## 6. Component responsibilities

| Component | Package | Responsibility | Does not do |
|-----------|---------|----------------|-------------|
| `ConfigLoader` | `config` | Resolve settings using the documented priority order | Know HTTP verbs or assertions |
| `FrameworkConfig` | `config` | Immutable typed snapshot of the active environment | Reload per request |
| `AuthStrategy` | `authentication` | Apply one auth mechanism to a request spec | Fetch tokens unless it is a token strategy |
| `TokenProvider` | `authentication` | Obtain and cache a token per thread when required | Refresh-token rotation (out of scope) |
| `RequestSpecFactory` | `specifications` | Base URI, timeouts, headers, filters, auth | Send requests |
| `ResponseSpecFactory` | `specifications` | Optional reusable expected content-type / success ranges | Business body assertions |
| `RestClient` | `clients` | GET, POST, PUT, PATCH, DELETE returning a response | Assert status or body |
| Application clients | `demo.booker.clients` | Endpoint paths, payloads, path/query params for one API | Generic HTTP plumbing |
| `JsonCodec` | `serialization` | Jackson serialize / deserialize | File I/O mixed with HTTP |
| `SensitiveDataRedactor` | `logging` | Mask tokens, passwords, API keys in logs and reports | Decide test pass/fail |
| `BaseApiTest` | `api.base` | TestNG lifecycle, config bootstrap, per-test correlation id | Contain business tests |
| HTTP assertion helpers | `api.assertions` | Status, header, content-type, response time, schema | Domain field equality |
| JSON DataProviders | `api.dataproviders` | Load parameterized JSON into TestNG | Hide expected results |
| Workflows | `demo.booker.workflows` | Multi-call business journeys with cross-call assertions | Independent unrelated requests |

---

## 7. Execution lifecycle

```text
Suite start
  └─ resolve env (-Denv / ENV / default qa)
  └─ load FrameworkConfig (immutable)
  └─ configure Log4j2 and Allure environment properties
  └─ build shared RequestSpecFactory settings (not a global mutable RestAssured state bag)

Test start
  └─ correlation id (UUID) bound to the thread
  └─ auth strategy applied (token fetched into ThreadLocal if needed)
  └─ application client created with the thread's request specification

Test body
  └─ client sends request via RestClient
  └─ logging filter records redacted request/response
  └─ Allure filter attaches the HTTP exchange
  └─ test asserts explicitly

Test end
  └─ cleanup created records where the API allows it
  └─ clear thread-local token / correlation id

Suite end
  └─ write Allure environment information
  └─ Surefire / Allure results remain under target/ (gitignored)
```

---

## 8. Configuration strategy

Environment names: `dev`, `qa`, `sit`, `uat`. Default: `qa`.

Selection:

```text
-Denv=qa                         first
ENV=qa                           second
config/framework.properties env  third
hardcoded default                qa
```

Value resolution for each setting:

```text
1. JVM system property          -Dapi.baseUri=https://...
2. Environment variable         API_BASE_URI
3. Environment file             src/test/resources/config/env/{env}.properties
4. Framework defaults           src/test/resources/config/framework.properties
```

Environment variable names are the property key in upper snake case (`api.baseUri` → `API_BASE_URI`, `http.connectTimeoutMs` → `HTTP_CONNECT_TIMEOUT_MS`). A present blank system property clears a file value, which is how a local password can be prevented from leaking into a CI run.

No dotenv library in the initial design. GitHub Actions and local shells already provide environment variables. An `.env.example` will be added only if a later phase introduces explicit `.env` loading.

Committed files must not contain real secrets. Restful Booker’s documented public demo credentials may appear in example comments or in a clearly labelled public-demo property file, because they are part of the API’s public documentation, not private credentials.

---

## 9. Authentication strategy

Extensible `AuthStrategy` applied by `RequestSpecFactory`:

| Strategy | Typical header / mechanism | Initial need |
|----------|----------------------------|--------------|
| `NoAuth` | none | Public GET endpoints |
| `BasicAuth` | HTTP Basic | Generic reuse |
| `BearerTokenAuth` | `Authorization: Bearer` | JWT / DummyJSON-style APIs |
| `ApiKeyAuth` | configurable header | Generic reuse |
| `CookieTokenAuth` | `Cookie: token=` | Restful Booker write operations |
| `OAuth2ClientCredentialsAuth` | token endpoint → Bearer | Interface ready; implement only if the chosen demo API needs it |

Token caching is `ThreadLocal`, not a static shared token. Parallel tests must not steal or overwrite each other’s tokens.

Refresh-token rotation is out of scope unless the demonstration API requires it. Restful Booker tokens are short-lived session cookies issued by `POST /auth`; they will be requested per thread, not refreshed.

---

## 10. Test data strategy

| Kind | Location | Use |
|------|----------|-----|
| Typed request/response models | `demo.booker.models` | Compile-time contracts; Java 17 records |
| Static JSON payloads | `src/test/resources/testdata/` | Valid, invalid, missing-field, boundary sets |
| TestNG DataProviders | `api.dataproviders` | Parameterized positive and negative cases |
| Dynamic isolation data | generated in tests/workflows | Unique names / timestamps so parallel runs do not collide |

Untyped `Map<String, Object>` payloads are allowed only when the point of the test is malformed JSON or a field that the typed model cannot represent.

JSON Schema files live under `src/test/resources/schemas/` and are asserted in tests that care about contract shape.

---

## 11. Assertion architecture

Three layers, kept distinct:

1. **HTTP-level helpers** — status code, content-type, response time, header presence, schema match.
2. **REST Assured / Hamcrest matchers** — JSONPath and collection checks where they are the clearest expression.
3. **Business assertions in the test or application assertion class** — “the booking dates round-trip”, “the deleted id is gone”.

Infrastructure must not swallow assertion failures or retry them.

---

## 12. Demonstration API

**Recommended target: Restful Booker**  
Base URI: `https://restful-booker.herokuapp.com`  
Docs: https://restful-booker.herokuapp.com/apidoc/index.html

Why this API:

- Public, documented, designed for API testing.
- Real CRUD with persistence (until the 10-minute reset).
- Authentication required for update and delete (`POST /auth` then cookie token).
- Supports a genuine chained workflow: create booking → read back → update → delete → verify absence.
- Health endpoint `GET /ping` (currently reachable; responds `Created`).
- No registration, no paid key, no destructive access to a real production business.

Known limitations that tests and docs must treat honestly:

| Limitation | Consequence |
|------------|-------------|
| Hosted on Heroku | Cold starts and occasional downtime. CI smoke must fail clearly, not hang. |
| Data reset about every 10 minutes | Tests must create their own bookings. Never depend on booking id `1`. |
| Documented quirks / bugs | Successful DELETE returns `201`. Auth for writes is cookie-based, not Bearer. `Accept` must be exactly `application/json` or POST returns **418 I'm a Teapot**. Some filter/date behaviours are wrong. Tests encode actual behaviour. |
| Public shared dataset | Parallel-safe unique payloads and cleanup after write tests. |
| Not a SLA-backed service | A later optional WireMock/contract suite may isolate CI from public-internet flake. Not in Phase 1. |

DummyJSON remains the documented fallback if Restful Booker proves too unstable for CI. DummyJSON has richer auth (JWT) and OpenAPI, but most writes are simulated and do not persist, which weakens chaining.

---

## 13. Reporting and logging

- **Logs:** SLF4J API + Log4j2. Request and response bodies logged at DEBUG, always through the redactor.
- **Correlation id:** one UUID per test, logged on every request.
- **Reports:** Allure via TestNG listener and `AllureRestAssured` filter.
- **Artifacts:** request/response attachments, environment name, base URI, Java version, TestNG suite name.
- **Redaction:** `Authorization`, `Cookie` token values, `password`, `token`, `accessToken`, `refreshToken`, API keys.

Reports must answer: which API, which request, which response, which assertion, expected vs actual, and whether the likely cause is application, environment, data or framework.

---

## 14. Execution model (to be implemented in Phase 8)

Intended commands. They will not be documented as supported until they actually work.

```text
mvn clean test
mvn clean test -Denv=qa
mvn clean test -Psmoke
mvn clean test -Pregression
mvn clean test -Dgroups=smoke
```

Suites: smoke, regression, negative. Groups on TestNG methods must match suite XML. Parallel mode: `tests` or `methods` with a modest thread count, after thread-safety of config and tokens is proven.

---

## 15. CI/CD approach (Phase 10)

GitHub Actions, not local-only scripts.

| Workflow | Trigger | Job |
|----------|---------|-----|
| CI smoke | pull request and push to `main` | Temurin 17, `mvn -B clean test -Psmoke` |
| Regression | `workflow_dispatch` and optionally schedule | `mvn -B clean test -Pregression` |
| Artifacts | always on test completion | Surefire reports, `target/allure-results` |

CI uses repository secrets for any non-public credential. Restful Booker demo credentials are public and do not belong in GitHub Secrets. JDK 17 is pinned in the workflow even if a developer’s machine uses JDK 21.

CI will not be described as “working” until a real workflow run on GitHub succeeds.

---

## 16. Extending the framework to a second API

To add another REST application later:

1. Add `config/env/*` entries or a second config prefix for its base URI.
2. Create `com.ashishqa.demo.<app>/clients|models|tests|workflows`.
3. Reuse `RestClient`, `RequestSpecFactory`, `AuthStrategy`, logging and Allure filters.
4. Add TestNG suite XML and testdata/schemas folders.

No change to `com.ashishqa.api` should be required unless the new API introduces a genuinely new transport concern.

---

## 17. Quality rules (non-negotiable)

1. No hardcoded base URIs in tests.
2. No credentials in source or committed config.
3. No assertions inside `RestClient`.
4. No shared mutable static tokens or request specs across threads.
5. No blanket retry analyzer on assertion failure.
6. No catching exceptions only to log and continue.
7. No duplicate HTTP wrappers.
8. Do not copy classes from the Selenium UI framework.
9. Do not claim an unrun Maven command succeeded.
10. Do not document a feature before it is implemented and validated.
