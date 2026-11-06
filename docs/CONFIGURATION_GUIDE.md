# Configuration guide

Settings are read by `com.ashishqa.api.config.ConfigManager`. Tests take the base URI from that snapshot. A test does not hardcode `https://restful-booker.herokuapp.com`.

## Where values come from

For each key, the first present value wins:

1. JVM system property, for example `-Dapi.baseUri=https://example.test`.
2. Environment variable. The name is the key in upper snake case, with a new word split before each capital: `api.baseUri` becomes `API_BASE_URI`, `http.connectTimeoutMs` becomes `HTTP_CONNECT_TIMEOUT_MS`.
3. `src/test/resources/config/env/{env}.properties`.
4. `src/test/resources/config/framework.properties`.

A present blank system property or environment variable replaces a file value, including replacing it with blank. There is no dotenv loader. `.env` is gitignored and is not read.

## Choosing the environment

Supported names: `dev`, `qa`, `sit`, `uat`. The default is `qa`.

Resolution order for the name itself:

1. `-Denv=qa`
2. Environment variable `ENV`
3. `env` in `framework.properties`
4. Hardcoded default `qa`

An unknown name fails the run with `ConfigurationException`.

| File | Base URI in the repository | Auth |
|------|----------------------------|------|
| `config/env/qa.properties` | `https://restful-booker.herokuapp.com` | `cookieToken`, published demo `admin` / `password123` |
| `config/env/dev.properties` | `http://localhost:8080` | `none` |
| `config/env/sit.properties` | `https://sit.example.invalid` | `none` |
| `config/env/uat.properties` | `https://uat.example.invalid` | `none` |

`qa.properties` is the only committed file with a username and password, and those values are the ones Restful Booker publishes in its own documentation. Point a run at another host without editing the file:

```text
mvn -B clean test "-Dapi.baseUri=https://example.test" "-Dauth.password="
```

`AUTH_PASSWORD` is the environment-variable form of `auth.password`.

## Keys

| Key | Environment variable | Role |
|-----|----------------------|------|
| `env` | `ENV` | Environment name |
| `api.baseUri` | `API_BASE_URI` | Required. Scheme and host. |
| `api.basePath` | `API_BASE_PATH` | Optional path prefix |
| `http.connectTimeoutMs` | `HTTP_CONNECT_TIMEOUT_MS` | Connect timeout |
| `http.responseTimeoutMs` | `HTTP_RESPONSE_TIMEOUT_MS` | Response timeout |
| `http.logRequests` | `HTTP_LOG_REQUESTS` | `true` or `false` |
| `http.logResponses` | `HTTP_LOG_RESPONSES` | `true` or `false` |
| `auth.type` | `AUTH_TYPE` | `none`, `basic`, `bearer`, `apiKey`, `cookieToken` |
| `auth.username` | `AUTH_USERNAME` | Basic auth, or the auth-endpoint user |
| `auth.password` | `AUTH_PASSWORD` | Basic auth, or the auth-endpoint password |
| `auth.token` | `AUTH_TOKEN` | Static bearer or cookie token |
| `auth.apiKeyHeader` | `AUTH_API_KEY_HEADER` | Default `X-Api-Key` |
| `auth.apiKey` | `AUTH_API_KEY` | API key value |
| `auth.tokenEndpoint` | `AUTH_TOKEN_ENDPOINT` | Path that issues a token. QA uses `/auth`. |
| `auth.tokenField` | `AUTH_TOKEN_FIELD` | JSON field that holds the token. QA uses `token`. |

`framework.properties` sets connect timeout 10000 ms, response timeout 30000 ms, logging on, and `auth.type=none`. The QA file raises the timeouts to 15000 ms and 45000 ms and sets `auth.type=cookieToken`.

## Auth behaviour

`AuthStrategies` maps `auth.type` onto `NoAuth`, `BasicAuth`, `BearerTokenAuth`, `ApiKeyAuth`, or `CookieTokenAuth`.

Restful Booker write calls need `Cookie: token=<value>`. `CookieTokenAuth` obtains that value from `ThreadLocalTokenProvider`, which calls `auth.tokenEndpoint` and reads `auth.tokenField`. Each test thread keeps its own token. `BaseApiTest` clears it after the test method.

`Authorization`, `Cookie`, `Set-Cookie`, `Proxy-Authorization`, and `X-Api-Key` are masked in Allure attachments. `SensitiveDataRedactor` masks `password`, `token`, `accessToken`, `refreshToken`, and API keys in logs and attachment bodies.

## Allure results directory

`src/test/resources/allure.properties` sets `allure.results.directory=target/allure-results`. Surefire also passes that directory as `allure.results.directory`. `AllureEnvironmentListener` writes `environment.properties` there at suite start. The file contains the environment name, base URI, Java version, OS, and suite name.
