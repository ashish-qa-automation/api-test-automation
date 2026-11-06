# Contributing

This repository is a public demonstration of a REST API test framework. Changes should keep the framework reusable and the Restful Booker demo honest about that API's real responses.

The license is the MIT license in `LICENSE` (Copyright 2026 Ashish Kumar). Keep that file as it is.

## Build requirements

JDK 17 or newer, and Maven 3.8.1 or newer. Confirm a change with a command that this repository already uses:

```text
mvn -B clean test
```

See [EXECUTION_GUIDE.md](EXECUTION_GUIDE.md) for suite profiles and groups.

## Where code goes

| Kind of change | Package |
|----------------|---------|
| HTTP, config, auth, logging, reporting | `com.ashishqa.api` |
| Restful Booker clients, models, tests, workflows | `com.ashishqa.demo.booker` |

Send HTTP only through `com.ashishqa.api.clients.RestClient` and `ApiRequest`. A new endpoint belongs on an application client such as `BookingClient`, which calls `RestClient`. Tests do not call `RestAssured.given()`, and they do not add a second HTTP wrapper.

`RestClient` returns the response. Status and body assertions stay in the test, in `HttpAssert`, or in a demo assertion such as `BookingAssert`. Do not add a TestNG retry analyzer for assertion failures.

Read base URI, timeouts, and auth from `ConfigManager`. Select the environment with `-Denv`. Supported names are `dev`, `qa`, `sit`, and `uat`.

## Authentication

Choose an `AuthStrategy`. Restful Booker writes use `CookieTokenAuth`. Bearer auth is available for other APIs and is the wrong mechanism for Booker updates and deletes.

Keep tokens on the calling thread. `ThreadLocalTokenProvider` already does this. Do not store a token in a static field shared across tests.

## Secrets and reports

`src/test/resources/config/env/qa.properties` may contain Restful Booker's published demo `admin` / `password123` only. Any other secret is supplied with `-Dauth.password` or `AUTH_PASSWORD`, not committed.

Mask logs and attachments with `com.ashishqa.api.logging.SensitiveDataRedactor`. Allure HTTP attachments go through `com.ashishqa.api.reporting.RedactingAllureFilter`. Do not attach a raw `Cookie`, `Authorization`, `password`, or `token`. New request logging keeps the per-test id from `com.ashishqa.api.logging.CorrelationId`.

## Tests

- Extend `BaseApiTest` for a test that needs config, a correlation id, and token cleanup.
- Booker tests that create bookings extend `BookerApiTest`, which deletes tracked ids after the method.
- Tag a method with `smoke`, `regression`, or `negative` to match the suite that should run it.
- Create the booking the test needs. Do not depend on booking id 1.
- Encode Restful Booker's actual status codes. Delete of an existing booking returns 201. A missing GET returns 404. A write to an unknown id returns 405. An invalid create returns 500.

## CI

`.github/workflows/ci.yml` and `.github/workflows/regression.yml` are the workflow files. Do not add a status badge until a run on `ashish-qa-automation/api-test-automation` has passed. Do not describe a Maven command or a GitHub Actions run as successful unless that run finished.

## Out of scope

Publishing to Maven Central, GraphQL, gRPC, SOAP, WebSocket, performance tests, OAuth2 authorization-code and refresh-token flows, and a retry of failed assertions. The Selenium UI framework in a sibling directory is a separate project. Do not copy its classes into this one.
