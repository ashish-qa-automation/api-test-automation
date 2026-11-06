package com.ashishqa.demo.booker.assertions;

import com.ashishqa.api.assertions.HttpAssert;
import com.ashishqa.demo.booker.models.Booking;
import io.restassured.response.Response;
import org.testng.Assert;

/**
 * Contract checks for successful Restful Booker JSON bodies.
 */
public final class BookingAssert {

    private BookingAssert() {
    }

    public static void createdMatchesSchema(Response response) {
        HttpAssert.statusIs(response, 200);
        HttpAssert.contentTypeIsJson(response);
        HttpAssert.matchesJsonSchema(response, BookerSchemas.CREATED_BOOKING);
    }

    public static void bookingMatchesSchema(Response response) {
        HttpAssert.statusIs(response, 200);
        HttpAssert.contentTypeIsJson(response);
        HttpAssert.matchesJsonSchema(response, BookerSchemas.BOOKING);
    }

    public static void bookingIdsMatchSchema(Response response) {
        HttpAssert.statusIs(response, 200);
        HttpAssert.contentTypeIsJson(response);
        HttpAssert.matchesJsonSchema(response, BookerSchemas.BOOKING_IDS);
    }

    public static void authTokenMatchesSchema(Response response) {
        HttpAssert.statusIs(response, 200);
        HttpAssert.contentTypeIsJson(response);
        HttpAssert.matchesJsonSchema(response, BookerSchemas.AUTH_TOKEN);
    }

    public static void fieldsMatch(Booking actual, Booking expected) {
        Assert.assertEquals(actual, expected, "Booking fields diverged across calls");
    }
}
