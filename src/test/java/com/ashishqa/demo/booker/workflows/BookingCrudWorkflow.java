package com.ashishqa.demo.booker.workflows;

import com.ashishqa.api.assertions.HttpAssert;
import com.ashishqa.demo.booker.assertions.BookerErrorAssert;
import com.ashishqa.demo.booker.assertions.BookingAssert;
import com.ashishqa.demo.booker.clients.BookingClient;
import com.ashishqa.demo.booker.models.Booking;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Create is the caller's job so the caller can track the id and delete it if a later step fails.
 *
 * <p>Restful Booker behaviour this journey asserts, rather than the behaviour a stricter API would have:
 * PUT and PATCH require {@code Cookie: token=...}. PATCH sends only the changed fields and returns the
 * full booking. A successful DELETE returns HTTP 201 with the body {@code Created}. The following GET
 * is 404.
 */
public final class BookingCrudWorkflow {

    private final BookingClient readers;
    private final BookingClient writers;

    public BookingCrudWorkflow(BookingClient readers, BookingClient writers) {
        this.readers = Objects.requireNonNull(readers, "readers");
        this.writers = Objects.requireNonNull(writers, "writers");
    }

    public void verifyUpdatePatchAndDelete(int bookingId, Booking current, Booking replacement, String patchedFirstname) {
        BookingAssert.fieldsMatch(read(bookingId), current);

        Response updated = writers.updateBooking(bookingId, replacement);
        HttpAssert.statusIs(updated, 200);
        HttpAssert.contentTypeIsJson(updated);
        BookingAssert.fieldsMatch(updated.as(Booking.class), replacement);
        BookingAssert.fieldsMatch(read(bookingId), replacement);

        Response patched = writers.patchBooking(bookingId, Map.of("firstname", patchedFirstname));
        HttpAssert.statusIs(patched, 200);
        HttpAssert.contentTypeIsJson(patched);
        Booking afterPatch = replacement.withFirstname(patchedFirstname);
        BookingAssert.fieldsMatch(patched.as(Booking.class), afterPatch);
        BookingAssert.fieldsMatch(read(bookingId), afterPatch);

        Response deleted = writers.deleteBooking(bookingId);
        HttpAssert.statusIs(deleted, 201);
        Assert.assertTrue(
                deleted.asString().toLowerCase(Locale.ROOT).contains("created"),
                "DELETE should return a Created body on Restful Booker. body=" + deleted.asString());
        BookerErrorAssert.notFound(readers.getBooking(bookingId));
    }

    private Booking read(int bookingId) {
        Response response = readers.getBooking(bookingId);
        BookingAssert.bookingMatchesSchema(response);
        return response.as(Booking.class);
    }
}
