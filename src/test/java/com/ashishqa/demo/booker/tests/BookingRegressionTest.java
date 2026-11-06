package com.ashishqa.demo.booker.tests;

import com.ashishqa.demo.booker.assertions.BookerErrorAssert;
import com.ashishqa.demo.booker.assertions.BookingAssert;
import com.ashishqa.demo.booker.clients.BookingClient;
import com.ashishqa.demo.booker.models.Booking;
import com.ashishqa.demo.booker.models.BookingDates;
import com.ashishqa.demo.booker.models.CreatedBooking;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * Cases beyond the smoke journey. Status codes are the ones Restful Booker returns today:
 * unknown-id writes are 405, not 404; a negative price and a checkout before checkin are stored.
 */
public class BookingRegressionTest extends BookerApiTest {

    @Test(groups = {"regression", "positive"})
    public void nameFilterReturnsTheCreatedBooking() {
        Booking booking = BookingExamples.standardStay().isolated();
        CreatedBooking created = createTracked(booking);

        Response found = publicBookings().findBookings(booking.firstname(), booking.lastname());
        BookingAssert.bookingIdsMatchSchema(found);
        List<Integer> ids = found.jsonPath().getList("bookingid", Integer.class);
        Assert.assertTrue(
                ids.contains(created.bookingid()),
                "firstname/lastname filter missed booking " + created.bookingid() + ". body=" + found.asString());
    }

    @Test(groups = {"regression", "negative"})
    public void unauthenticatedUpdateAndPatchLeaveTheBookingUnchanged() {
        Booking booking = BookingExamples.standardStay().isolated();
        CreatedBooking created = createTracked(booking);
        BookingClient anonymous = publicBookings();

        BookerErrorAssert.forbidden(
                anonymous.updateBookingAnonymously(created.bookingid(), booking.withFirstname("Hacker")));
        BookerErrorAssert.forbidden(
                anonymous.patchBookingAnonymously(created.bookingid(), Map.of("firstname", "Hacker")));

        Response reread = anonymous.getBooking(created.bookingid());
        BookingAssert.bookingMatchesSchema(reread);
        BookingAssert.fieldsMatch(reread.as(Booking.class), booking);
    }

    @Test(groups = {"regression", "negative"})
    public void writesToAnUnknownBookingAreMethodNotAllowed() {
        int missingId = 2_147_483_646;
        BookingClient writers = authenticatedBookings();
        Booking payload = BookingExamples.standardStay();

        BookerErrorAssert.methodNotAllowed(writers.updateBooking(missingId, payload));
        BookerErrorAssert.methodNotAllowed(writers.patchBooking(missingId, Map.of("firstname", "Nobody")));
        BookerErrorAssert.methodNotAllowed(writers.deleteBooking(missingId));
    }

    @Test(groups = {"regression", "boundary"})
    public void negativePriceAndInvertedDatesAreStoredAsSent() {
        Booking booking = new Booking(
                "Edge",
                "Case",
                -1,
                false,
                new BookingDates("2026-08-10", "2026-08-01"),
                "None")
                .isolated();
        CreatedBooking created = createTracked(booking);
        BookingAssert.fieldsMatch(created.booking(), booking);

        Response reread = publicBookings().getBooking(created.bookingid());
        BookingAssert.bookingMatchesSchema(reread);
        BookingAssert.fieldsMatch(reread.as(Booking.class), booking);
    }
}
