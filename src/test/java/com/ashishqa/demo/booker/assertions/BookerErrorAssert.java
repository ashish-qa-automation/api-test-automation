package com.ashishqa.demo.booker.assertions;

import com.ashishqa.api.assertions.HttpAssert;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.Locale;

/**
 * Restful Booker error shapes as the API actually behaves, not RFC 7807 problem details.
 */
public final class BookerErrorAssert {

    private BookerErrorAssert() {
    }

    public static void forbidden(Response response) {
        HttpAssert.statusIs(response, 403);
        assertPlainTextMentions(response, "forbidden");
    }

    public static void notFound(Response response) {
        HttpAssert.statusIs(response, 404);
        assertPlainTextMentions(response, "not found");
    }

    /**
     * PUT, PATCH and DELETE of an id that does not exist return 405, not 404.
     * GET of a missing id is the call that returns 404.
     */
    public static void methodNotAllowed(Response response) {
        HttpAssert.statusIs(response, 405);
        assertPlainTextMentions(response, "method not allowed");
    }

    /**
     * Invalid creates return HTTP 500 with a plain-text body, not 400.
     */
    public static void invalidCreate(Response response) {
        HttpAssert.statusIs(response, 500);
    }

    public static void teapot(Response response) {
        HttpAssert.statusIs(response, 418);
    }

    /**
     * Failed {@code POST /auth} returns HTTP 200 and {@code {"reason":"Bad credentials"}}.
     */
    public static void badCredentials(Response response) {
        HttpAssert.statusIs(response, 200);
        HttpAssert.contentTypeIsJson(response);
        HttpAssert.matchesJsonSchema(response, BookerSchemas.AUTH_FAILURE);
        Assert.assertEquals(
                response.jsonPath().getString("reason"),
                "Bad credentials",
                "Restful Booker failed login uses reason=Bad credentials");
    }

    private static void assertPlainTextMentions(Response response, String expected) {
        String body = response.asString();
        Assert.assertNotNull(body, "Error body was empty. status=" + response.statusCode());
        Assert.assertTrue(
                body.toLowerCase(Locale.ROOT).contains(expected),
                "Expected error body to mention '" + expected + "', was: " + body);
    }
}
