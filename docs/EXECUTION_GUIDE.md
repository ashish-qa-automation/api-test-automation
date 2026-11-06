# Execution guide

How to run the suites in this repository. Commands in the "Commands that have passed" section are the ones recorded as BUILD SUCCESS. Details of configuration keys are in [CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md).

## Prerequisites

- JDK 17 or newer. The compiler target is `--release 17`. Maven on a machine using JDK 21 can build this project. JDK 11 cannot.
- Apache Maven 3.8.1 or newer. The enforcer plugin checks both versions.
- Network access to `https://restful-booker.herokuapp.com` for the live booking tests. Framework unit tests do not need that host.

Run every command from the repository root.

## Default run

```text
mvn -B clean test
```

Surefire loads `src/test/resources/suites/smoke.xml`. The environment is `qa` unless `-Denv` or `ENV` selects another name. On 6 Nov 2026, after reporting was added, this command ran 66 tests and 0 failures.

## Commands that have passed

Recorded on 6 Nov 2026:

```text
mvn -B clean test
mvn -B clean test -Denv=qa
mvn -B clean test -Psmoke
mvn -B clean test -Pregression
mvn -B clean test -Pnegative
mvn -B clean test -Dgroups=smoke
mvn -B clean test -Dgroups=negative
```

Counts from the Phase 8 run, before the Allure tests were added:

| Command | Tests |
|---------|-------|
| `mvn -B clean test`, `-Psmoke`, `-Dgroups=smoke`, `-Denv=qa` | 58 |
| `mvn -B clean test -Pregression` | 5 |
| `mvn -B clean test -Pnegative` | 9 |
| `mvn -B clean test -Dgroups=negative` | 7, on the smoke suite |
| `mvn -B clean test -Pregression -Dgroups=smoke` | 1, the CRUD workflow |

The later `mvn -B clean test` count is 66. The profile and group commands were not re-run after that reporting change.

On PowerShell, quote a property that contains `=`, for example `mvn -B clean test "-Denv=qa"`.

## Suites

All three suites use `parallel="methods"`, `thread-count="3"`, and `data-provider-thread-count="1"`. There is no retry analyzer.

| Profile | Suite file | What it selects |
|---------|------------|-----------------|
| default and `-Psmoke` | `suites/smoke.xml` | Classes in the smoke group, including framework unit tests and the booking smoke tests |
| `-Pregression` | `suites/regression.xml` | `BookingCrudWorkflowTest` and `BookingRegressionTest`, groups `smoke` and `regression` |
| `-Pnegative` | `suites/negative.xml` | Package `com.ashishqa.demo.booker.tests`, group `negative` |

`-Dgroups=smoke` is Surefire's TestNG group filter. It replaces the groups included by the suite file for that run. An empty `groups` value is not set in the pom, because an empty include list can match nothing.

## Groups

| Group | Role |
|-------|------|
| `smoke` | Default suite. Includes the CRUD workflow. |
| `regression` | Extra booking checks: name filter, unauthenticated updates, unknown id, stored boundary values. |
| `negative` | Rejected auth, invalid payloads, missing bookings, and the negative methods on the regression class. |

## Environment

```text
mvn -B clean test -Denv=qa
```

Supported names are `dev`, `qa`, `sit`, and `uat`. `qa` is the public Restful Booker sandbox. `dev` points at `http://localhost:8080`. `sit` and `uat` point at placeholder hosts and are not live targets.

## What a live booking test does

Write tests create their own bookings and delete them afterwards. They do not depend on booking id 1. Restful Booker resets its data about every 10 minutes.

Observed contract, encoded in the tests:

- `GET /ping` returns 201 with body `Created`.
- Create returns 200.
- Delete of an existing booking returns 201 with body `Created`.
- Unauthenticated PUT, PATCH, and DELETE return 403.
- GET of a missing id returns 404.
- PUT, PATCH, and DELETE of an unknown id return 405.
- An invalid or empty create returns 500 with a plain-text body.
- Failed auth returns HTTP 200 and `{"reason":"Bad credentials"}`.
- PATCH sends a partial body and returns the full booking.
- A negative `totalprice` and a checkout before checkin are stored.
- `Accept` on create is the literal `application/json`.

## Results

| Output | Path |
|--------|------|
| Surefire | `target/surefire-reports` |
| Allure results | `target/allure-results` |
| Allure environment file | `target/allure-results/environment.properties` |

`environment.properties` records the environment name, base URI, Java version, OS, and suite name. It does not record passwords or tokens. HTTP attachments are written by `RedactingAllureFilter`. The Allure Maven plugin reads `target/allure-results` and writes `target/allure-report`:

```text
mvn allure:report
```

`target/`, `allure-results/`, and `allure-report/` are gitignored.

## Logging

`log4j2-test.xml` sets `com.ashishqa.api.logging` to DEBUG. INFO lines include the correlation id, method, and URI. DEBUG lines include headers, cookies, and bodies after `SensitiveDataRedactor` masks them. Each test sends `X-Correlation-Id`.

## GitHub Actions

| Workflow | Trigger | Maven command |
|----------|---------|---------------|
| `.github/workflows/ci.yml` | pull request, push to `main` | `mvn -B clean test -Psmoke` |
| `.github/workflows/regression.yml` | manual, Monday 06:15 UTC | `mvn -B clean test -Pregression` |

Both use Temurin 17 on `ubuntu-latest`, time out at 20 minutes, and upload `target/surefire-reports` and `target/allure-results` even when Maven fails. These files are in the repository. GitHub has not run them yet.
