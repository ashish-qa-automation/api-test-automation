package com.ashishqa.demo.booker.tests;

import com.ashishqa.demo.booker.assertions.BookingAssert;
import com.ashishqa.demo.booker.models.Booking;
import com.ashishqa.demo.booker.models.CreatedBooking;
import com.ashishqa.demo.booker.workflows.BookingCrudWorkflow;
import org.testng.annotations.Test;

/**
 * One business journey: the booking created here is the booking read back, replaced, partially updated,
 * and then removed. The sandbox resets about every 10 minutes and is shared, so the firstname is unique
 * and {@link BookerApiTest} deletes the record if the journey stops early.
 */
public class BookingCrudWorkflowTest extends BookerApiTest {

    @Test(groups = "smoke")
    public void bookingCanBeCreatedReadUpdatedPatchedAndDeleted() {
        Booking initial = BookingExamples.standardStay().isolated();
        CreatedBooking created = createTracked(initial);
        BookingAssert.fieldsMatch(created.booking(), initial);

        Booking replacement = initial.withFirstname(initial.firstname() + "U");
        new BookingCrudWorkflow(publicBookings(), authenticatedBookings())
                .verifyUpdatePatchAndDelete(
                        created.bookingid(),
                        initial,
                        replacement,
                        initial.firstname() + "P");
        untrack(created.bookingid());
    }
}
