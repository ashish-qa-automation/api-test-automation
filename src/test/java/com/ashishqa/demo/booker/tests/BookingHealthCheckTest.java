package com.ashishqa.demo.booker.tests;

import com.ashishqa.api.base.BaseApiTest;
import com.ashishqa.api.specifications.ResponseSpecFactory;
import com.ashishqa.demo.booker.clients.BookingClient;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Live connectivity check against Restful Booker {@code GET /ping}.
 * The API documents a 201 Created body of {@code Created}, not HTTP 200.
 */
public class BookingHealthCheckTest extends BaseApiTest {

    private static final Logger log = LoggerFactory.getLogger(BookingHealthCheckTest.class);

    @Test(groups = "smoke")
    public void pingReturnsCreated() {
        BookingClient bookings = new BookingClient(config());
        log.info("Pinging {}{}", config().baseUri(), BookingClient.PING);
        Response response = bookings.ping();
        Assert.assertEquals(
                response.statusCode(),
                201,
                "GET " + config().baseUri() + BookingClient.PING
                        + " should return 201 Created. body=" + response.asString());
        response.then().spec(ResponseSpecFactory.statusCode(201));
        Assert.assertTrue(
                response.asString().toLowerCase().contains("created"),
                "Ping body should mention Created, was: " + response.asString());
    }
}
