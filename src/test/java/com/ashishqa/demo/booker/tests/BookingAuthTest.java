package com.ashishqa.demo.booker.tests;

import com.ashishqa.api.base.BaseApiTest;
import com.ashishqa.api.logging.SensitiveDataRedactor;
import com.ashishqa.demo.booker.assertions.BookerErrorAssert;
import com.ashishqa.demo.booker.clients.BookingClient;
import com.ashishqa.demo.booker.models.Booking;
import com.ashishqa.demo.booker.models.BookingDates;
import com.ashishqa.demo.booker.models.CreatedBooking;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Restful Booker write operations require a cookie token from {@code POST /auth}.
 */
public class BookingAuthTest extends BaseApiTest {

    @Test(groups = {"smoke", "negative"})
    public void deleteWithoutTokenIsRejected() {
        int bookingId = createIsolatedBooking();
        Response response = new BookingClient(anonymousRestClient()).deleteBookingAnonymously(bookingId);
        BookerErrorAssert.forbidden(response);
        new BookingClient(restClient()).deleteBooking(bookingId);
    }

    @Test(groups = "smoke")
    public void cookieTokenAllowsUpdateAndDelete() {
        int bookingId = createIsolatedBooking();
        BookingClient authenticated = new BookingClient(restClient());

        Booking updatedPayload = sampleBooking().withFirstname("Updated");
        Response updated = authenticated.updateBooking(bookingId, updatedPayload);
        Assert.assertEquals(
                updated.statusCode(),
                200,
                "Authenticated PUT /booking/{id} should succeed. body=" + updated.asString());
        Assert.assertEquals(updated.as(Booking.class).firstname(), "Updated");

        Response deleted = authenticated.deleteBooking(bookingId);
        Assert.assertEquals(
                deleted.statusCode(),
                201,
                "Authenticated DELETE /booking/{id} returns 201 on Restful Booker. body="
                        + deleted.asString());

        Response missing = new BookingClient(anonymousRestClient()).getBooking(bookingId);
        BookerErrorAssert.notFound(missing);
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
        CreatedBooking created = new BookingClient(anonymousRestClient()).createBookingTyped(sampleBooking());
        Assert.assertTrue(created.bookingid() > 0, "Create response must include bookingid");
        return created.bookingid();
    }

    private static Booking sampleBooking() {
        return new Booking(
                "Auth",
                "Check",
                120,
                true,
                new BookingDates("2026-04-01", "2026-04-05"),
                "None")
                .isolated();
    }
}
