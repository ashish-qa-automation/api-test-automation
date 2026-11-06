package com.ashishqa.demo.booker.assertions;

import io.restassured.builder.ResponseBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BookerErrorAssertTest {

    @Test(groups = "smoke")
    public void forbiddenAcceptsPlainTextBody() {
        BookerErrorAssert.forbidden(plain(403, "Forbidden"));
    }

    @Test(groups = "smoke")
    public void notFoundAcceptsPlainTextBody() {
        BookerErrorAssert.notFound(plain(404, "Not Found"));
    }

    @Test(groups = "smoke")
    public void invalidCreateExpectsServerError() {
        BookerErrorAssert.invalidCreate(plain(500, "Internal Server Error"));
    }

    @Test(groups = "smoke")
    public void teapotExpects418() {
        BookerErrorAssert.teapot(plain(418, "I'm a Teapot"));
    }

    @Test(groups = "smoke")
    public void methodNotAllowedExpects405() {
        BookerErrorAssert.methodNotAllowed(plain(405, "Method Not Allowed"));
    }

    @Test(groups = "smoke")
    public void badCredentialsExpectsHttp200WithReason() {
        BookerErrorAssert.badCredentials(json(200, "{\"reason\":\"Bad credentials\"}"));
    }

    @Test(groups = "smoke")
    public void forbiddenRejectsWrongStatus() {
        try {
            BookerErrorAssert.forbidden(plain(401, "Forbidden"));
            Assert.fail("Expected 401 not to satisfy forbidden");
        } catch (AssertionError ex) {
            Assert.assertTrue(ex.getMessage().contains("401"));
        }
    }

    private static Response plain(int status, String body) {
        return new ResponseBuilder()
                .setStatusCode(status)
                .setContentType("text/plain")
                .setBody(body)
                .build();
    }

    private static Response json(int status, String body) {
        return new ResponseBuilder()
                .setStatusCode(status)
                .setContentType(ContentType.JSON)
                .setBody(body)
                .build();
    }
}
