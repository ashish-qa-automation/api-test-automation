package com.ashishqa.api.assertions;

import com.ashishqa.api.logging.SensitiveDataRedactor;
import com.ashishqa.api.utilities.ClasspathResource;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * HTTP-level checks. Business field equality stays in the test or an application assertion class.
 */
public final class HttpAssert {

    private static final SensitiveDataRedactor REDACTOR = new SensitiveDataRedactor();
    private static final int MAX_BODY_CHARS = 300;

    private HttpAssert() {
    }

    public static void statusIs(Response response, int expectedStatus) {
        Objects.requireNonNull(response, "response");
        Assert.assertEquals(response.statusCode(), expectedStatus, "HTTP status. " + detail(response));
    }

    public static void statusIn(Response response, int... expectedStatuses) {
        Objects.requireNonNull(response, "response");
        if (expectedStatuses == null || expectedStatuses.length == 0) {
            throw new IllegalArgumentException("expectedStatuses is required");
        }
        int actual = response.statusCode();
        for (int expected : expectedStatuses) {
            if (actual == expected) {
                return;
            }
        }
        Assert.fail("HTTP status " + actual + " not in " + Arrays.toString(expectedStatuses) + ". " + detail(response));
    }

    public static void contentTypeIsJson(Response response) {
        Objects.requireNonNull(response, "response");
        String contentType = response.getContentType();
        Assert.assertNotNull(contentType, "Content-Type is missing. " + detail(response));
        Assert.assertTrue(
                contentType.toLowerCase(Locale.ROOT).contains("application/json"),
                "Expected JSON Content-Type, was '" + contentType + "'. " + detail(response));
    }

    public static void headerPresent(Response response, String name) {
        Objects.requireNonNull(response, "response");
        requireHeaderName(name);
        String value = response.getHeader(name);
        Assert.assertNotNull(value, "Missing header '" + name + "'. " + detail(response));
        Assert.assertFalse(value.isBlank(), "Header '" + name + "' is blank. " + detail(response));
    }

    public static void headerEquals(Response response, String name, String expectedValue) {
        headerPresent(response, name);
        Assert.assertEquals(
                response.getHeader(name),
                expectedValue,
                "Header '" + name + "'. " + detail(response));
    }

    public static void timeAtMost(Response response, long maxMillis) {
        Objects.requireNonNull(response, "response");
        if (maxMillis < 0) {
            throw new IllegalArgumentException("maxMillis must be >= 0");
        }
        long actual = response.getTime();
        Assert.assertTrue(
                actual <= maxMillis,
                "Response time " + actual + "ms exceeded " + maxMillis + "ms. " + detail(response));
    }

    public static void matchesJsonSchema(Response response, String classpathSchema) {
        Objects.requireNonNull(response, "response");
        String location = ClasspathResource.require(classpathSchema);
        response.then().assertThat().body(matchesJsonSchemaInClasspath(location));
    }

    private static void requireHeaderName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("header name is required");
        }
    }

    private static String detail(Response response) {
        String body = response.asString();
        if (body != null && body.length() > MAX_BODY_CHARS) {
            body = body.substring(0, MAX_BODY_CHARS) + "...";
        }
        return "status=" + response.statusCode() + " body=" + REDACTOR.redact(body);
    }
}
