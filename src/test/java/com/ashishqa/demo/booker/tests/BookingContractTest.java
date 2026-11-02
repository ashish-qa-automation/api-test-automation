package com.ashishqa.demo.booker.tests;

import com.ashishqa.api.base.BaseApiTest;
import com.ashishqa.demo.booker.assertions.BookerErrorAssert;
import com.ashishqa.demo.booker.assertions.BookingAssert;
import com.ashishqa.demo.booker.clients.AuthClient;
import com.ashishqa.demo.booker.clients.BookingClient;
import com.ashishqa.demo.booker.models.Booking;
import com.ashishqa.demo.booker.models.BookingDates;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Live JSON Schema and error-shape checks against Restful Booker.
 */
public class BookingContractTest extends BaseApiTest {

    @Test(groups = "smoke")
    public void createAndGetMatchBookingSchemas() {
        Booking payload = sampleBooking();
        BookingClient client = new BookingClient(anonymousRestClient());
        Response created = client.createBooking(payload);
        BookingAssert.createdMatchesSchema(created);
        int id = created.jsonPath().getInt("bookingid");
        try {
            BookingAssert.bookingMatchesSchema(client.getBooking(id));
        } finally {
            new BookingClient(restClient()).deleteBooking(id);
        }
    }

    @Test(groups = "smoke")
    public void bookingIdListMatchesSchema() {
        BookingAssert.bookingIdsMatchSchema(new BookingClient(anonymousRestClient()).getBookingIds());
    }

    @Test(groups = "smoke")
    public void authTokenMatchesSchema() {
        Response response = new AuthClient(anonymousRestClient())
                .createToken(config().authUsername(), config().authPassword());
        BookingAssert.authTokenMatchesSchema(response);
    }

    @Test(groups = "smoke")
    public void failedAuthReturnsBadCredentialsJson() {
        Response response = new AuthClient(anonymousRestClient())
                .createToken("not-a-user", "not-a-password");
        BookerErrorAssert.badCredentials(response);
    }

    @Test(groups = "smoke")
    public void missingBookingIsPlainTextNotFound() {
        BookerErrorAssert.notFound(new BookingClient(anonymousRestClient()).getBooking(Integer.MAX_VALUE));
    }

    @Test(groups = "smoke")
    public void emptyCreateIsServerError() {
        BookerErrorAssert.invalidCreate(
                new BookingClient(anonymousRestClient()).createBookingPayload(Map.of()));
    }

    private static Booking sampleBooking() {
        return new Booking(
                "Schema",
                "Check",
                88,
                true,
                new BookingDates("2026-05-01", "2026-05-03"),
                "None")
                .isolated();
    }
}
