# API Test Automation Framework

A Java 17 REST API test framework built with REST Assured and TestNG. The reusable core is `com.ashishqa.api`. The public [Restful Booker](https://restful-booker.herokuapp.com/apidoc/index.html) API is the demonstration application, under `com.ashishqa.demo.booker`.

HTTP goes through `RestClient` and `ApiRequest`. Settings come from `ConfigManager`. Restful Booker writes use `CookieTokenAuth`. Logs and Allure attachments mask passwords, tokens, and cookies.

License: [MIT](LICENSE).

## Stack

| Piece | Version |
|-------|---------|
| Java | 17 (`--release 17`). JDK 17 or newer is required. |
| Maven | 3.8.1 or newer |
| REST Assured | 6.2.0 |
| TestNG | 7.11.0 |
| Allure | 2.35.5 |
| Jackson | 2.18.2 |
| Logging | SLF4J 2.0.17 + Log4j2 2.25.5 |

Maven coordinates: `com.ashishqa:api-test-automation:1.0.0-SNAPSHOT`.

## Run

From the repository root, with JDK 17 or newer on the Maven toolchain:

```text
mvn -B clean test
```

That runs `src/test/resources/suites/smoke.xml`. The default environment is `qa`, which points at `https://restful-booker.herokuapp.com`.

These commands finished with BUILD SUCCESS on 6 Nov 2026:

```text
mvn -B clean test
mvn -B clean test -Denv=qa
mvn -B clean test -Psmoke
mvn -B clean test -Pregression
mvn -B clean test -Pnegative
mvn -B clean test -Dgroups=smoke
mvn -B clean test -Dgroups=negative
```

The latest full run, `mvn -B clean test`, was 66 tests and 0 failures. Suite counts and group behaviour are in [docs/EXECUTION_GUIDE.md](docs/EXECUTION_GUIDE.md). Property keys and override order are in [docs/CONFIGURATION_GUIDE.md](docs/CONFIGURATION_GUIDE.md).

## What the demonstration covers

- Health check, authentication, payload, and contract checks against Restful Booker.
- A create, read, update, partial update, delete, and confirm-gone workflow, with cleanup of created bookings.
- Parallel TestNG methods (thread count 3) and a per-thread auth token.
- JSON schema checks, redacted request logging, and redacted Allure HTTP attachments.
- A correlation id (`X-Correlation-Id`) on each test.

Restful Booker stores a successful delete as HTTP 201 with body `Created`. `Accept` on create must be exactly `application/json`. Writes to an unknown booking id return 405. `qa.properties` contains the vendor's published demo username `admin` and password `password123`. Override either value with `-Dauth.password` or `AUTH_PASSWORD`.

## Reports

Surefire writes `target/surefire-reports`. Allure results land in `target/allure-results`, including `environment.properties` (environment, base URI, Java, OS, suite name). The Allure Maven plugin is configured to render `target/allure-report` from those results:

```text
mvn allure:report
```

## Continuous integration

`.github/workflows/ci.yml` runs `mvn -B clean test -Psmoke` on pull requests and on pushes to `main`, using Temurin 17. `.github/workflows/regression.yml` runs `mvn -B clean test -Pregression` on demand and every Monday at 06:15 UTC. Both upload Surefire and Allure results.

GitHub has not run these workflows yet, so this README does not show a status badge.

## Layout

```text
src/main/java/com/ashishqa/api/          framework
src/test/java/com/ashishqa/demo/booker/  Restful Booker clients, models, tests
src/test/resources/config/               framework.properties and env/*.properties
src/test/resources/suites/               smoke.xml, regression.xml, negative.xml
docs/                                    architecture, execution, configuration
```

## Further reading

- [Architecture](docs/ARCHITECTURE.md)
- [Execution guide](docs/EXECUTION_GUIDE.md)
- [Configuration guide](docs/CONFIGURATION_GUIDE.md)
- [Contributing](docs/CONTRIBUTING.md)
- [Technical decisions](docs/TECHNICAL_DECISIONS.md)
