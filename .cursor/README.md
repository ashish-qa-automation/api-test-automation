# Cursor rules for this repository

These rules describe the code that is already in `api-test-automation`. They are not generic Java style guidance.

| Rule | When it applies |
|------|-----------------|
| `rules/framework.mdc` | Every session. `RestClient`, `AuthStrategy`, `-Denv`, and the Maven commands that have already passed. |
| `rules/secrets.mdc` | Every session. `SensitiveDataRedactor`, `RedactingAllureFilter`, and the public demo credentials in `qa.properties`. |
| `rules/api-tests.mdc` | While editing `src/test/java/**/*.java`. `BaseApiTest`, `BookerApiTest`, `BookingClient`, and `HttpAssert`. |

GitHub Actions workflows are in `.github/workflows/`. They have not run on GitHub, so the rules forbid a green badge until a real run passes.
