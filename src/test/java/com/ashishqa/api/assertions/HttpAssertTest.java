package com.ashishqa.api.assertions;

import io.restassured.builder.ResponseBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpAssertTest {

    @Test(groups = "smoke")
    public void statusAndHeaderHelpersPassOnMatchingResponse() {
        Response response = json(200, "{\"ok\":true}").build();
        HttpAssert.statusIs(response, 200);
        HttpAssert.statusIn(response, 200, 201);
        HttpAssert.contentTypeIsJson(response);
        HttpAssert.headerPresent(response, "X-Test");
        HttpAssert.headerEquals(response, "X-Test", "yes");
        HttpAssert.timeAtMost(response, 1_000);
    }

    @Test(groups = "smoke")
    public void statusMismatchFails() {
        Response response = json(404, "Not Found").build();
        try {
            HttpAssert.statusIs(response, 200);
            Assert.fail("Expected status assertion to fail");
        } catch (AssertionError ex) {
            Assert.assertTrue(ex.getMessage().contains("404"));
        }
    }

    @Test(groups = "smoke")
    public void jsonSchemaMatchPassesAndMismatchFails() {
        HttpAssert.matchesJsonSchema(json(200, "{\"ok\":true}").build(), "schemas/test/ok-object.json");
        try {
            HttpAssert.matchesJsonSchema(json(200, "{\"ok\":\"yes\"}").build(), "schemas/test/ok-object.json");
            Assert.fail("Expected schema mismatch");
        } catch (AssertionError ex) {
            Assert.assertNotNull(ex.getMessage());
        }
    }

    @Test(groups = "smoke")
    public void missingSchemaFailsBeforeValidation() {
        try {
            HttpAssert.matchesJsonSchema(json(200, "{}").build(), "schemas/test/does-not-exist.json");
            Assert.fail("Expected missing schema to fail");
        } catch (IllegalStateException ex) {
            Assert.assertTrue(ex.getMessage().contains("does-not-exist.json"));
        }
    }

    private static ResponseBuilder json(int status, String body) {
        return new ResponseBuilder()
                .setStatusCode(status)
                .setContentType(ContentType.JSON)
                .setHeader("X-Test", "yes")
                .setBody(body);
    }
}
