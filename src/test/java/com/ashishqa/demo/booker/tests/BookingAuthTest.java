package com.ashishqa.demo.booker.tests;

import com.ashishqa.api.base.BaseApiTest;
import com.ashishqa.api.logging.SensitiveDataRedactor;
import com.ashishqa.demo.booker.clients.BookingClient;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Restful Booker write operations require a cookie token from {@code POST /auth}.
 */
public class BookingAuthTest extends BaseApiTest {

    @Test(groups = "smoke")
    public void deleteWithoutTokenIsRejected() {
        int bookingId = createIsolatedBooking();
        Response response = new BookingClient(anonymousRestClient()).deleteBookingAnonymously(bookingId);
        Assert.assertEquals(
                response.statusCode(),
                403,
                "DELETE without token should be forbidden. body=" + response.asString());
        new BookingClient(restClient()).deleteBooking(bookingId);
    }

    @Test(groups = "smoke")
    public void cookieTokenAllowsUpdateAndDelete() {
        int bookingId = createIsolatedBooking();
        BookingClient authenticated = new BookingClient(restClient());

        Map<String, Object> patch = sampleBooking();
        patch.put("firstname", "Updated");
        Response updated = authenticated.updateBooking(bookingId, patch);
        Assert.assertEquals(
                updated.statusCode(),
                200,
                "Authenticated PUT /booking/{id} should succeed. body=" + updated.asString());
        Assert.assertEquals(updated.jsonPath().getString("firstname"), "Updated");

        Response deleted = authenticated.deleteBooking(bookingId);
        Assert.assertEquals(
                deleted.statusCode(),
                201,
                "Authenticated DELETE /booking/{id} returns 201 on Restful Booker. body="
                        + deleted.asString());

        Response missing = new BookingClient(anonymousRestClient()).getBooking(bookingId);
        Assert.assertEquals(missing.statusCode(), 404, "Deleted booking should no longer be readable");
    }

    @Test(groups = "smoke")
    public void redactorMasksAuthPayloadsBeforeTheyCanBeLogged() {
        SensitiveDataRedactor redactor = new SensitiveDataRedactor();
        String sample = "{\"username\":\"admin\",\"password\":\"password123\",\"token\":\"super-secret-token\"}";
        String redacted = redactor.redact(sample);
        Assert.assertFalse(redacted.contains("password123"));
        Assert.assertFalse(redacted.contains("super-secret-token"));
        Assert.assertTrue(redacted.contains("***"));
    }

    private int createIsolatedBooking() {
        Response created = new BookingClient(anonymousRestClient()).createBooking(sampleBooking());
        Assert.assertEquals(
                created.statusCode(),
                200,
                "POST /booking should create a record. body=" + created.asString());
        Integer id = created.jsonPath().getInt("bookingid");
        Assert.assertNotNull(id, "Create response must include bookingid");
        return id;
    }

    private static Map<String, Object> sampleBooking() {
        Map<String, Object> dates = new LinkedHashMap<>();
        dates.put("checkin", "2026-04-01");
        dates.put("checkout", "2026-04-05");
        Map<String, Object> booking = new LinkedHashMap<>();
        booking.put("firstname", "Auth" + UUID.randomUUID().toString().substring(0, 8));
        booking.put("lastname", "Check");
        booking.put("totalprice", 120);
        booking.put("depositpaid", true);
        booking.put("bookingdates", dates);
        booking.put("additionalneeds", "None");
        return booking;
    }
}
