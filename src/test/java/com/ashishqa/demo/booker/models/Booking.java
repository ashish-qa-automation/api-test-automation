package com.ashishqa.demo.booker.models;

import java.util.UUID;

public record Booking(
        String firstname,
        String lastname,
        Integer totalprice,
        Boolean depositpaid,
        BookingDates bookingdates,
        String additionalneeds
) {

    public Booking withFirstname(String firstname) {
        return new Booking(firstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
    }

    /**
     * Returns a copy with a unique firstname so parallel or repeated runs do not collide.
     */
    public Booking isolated() {
        String prefix = firstname == null || firstname.isBlank() ? "Auto" : firstname;
        String unique = prefix + UUID.randomUUID().toString().substring(0, 8);
        if (unique.length() > 40) {
            unique = unique.substring(0, 40);
        }
        return withFirstname(unique);
    }
}
