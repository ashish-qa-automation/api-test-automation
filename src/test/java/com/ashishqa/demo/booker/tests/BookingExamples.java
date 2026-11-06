package com.ashishqa.demo.booker.tests;

import com.ashishqa.demo.booker.models.Booking;
import com.ashishqa.demo.booker.models.BookingDates;

/**
 * Baseline booking used by the demonstration suite. Call {@link Booking#isolated()} before a live create.
 */
public final class BookingExamples {

    private BookingExamples() {
    }

    public static Booking standardStay() {
        return new Booking(
                "Ada",
                "Lovelace",
                210,
                true,
                new BookingDates("2026-07-01", "2026-07-04"),
                "Breakfast");
    }
}
