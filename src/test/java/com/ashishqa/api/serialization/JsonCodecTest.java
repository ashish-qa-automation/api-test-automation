package com.ashishqa.api.serialization;

import com.ashishqa.demo.booker.models.Booking;
import com.ashishqa.demo.booker.models.BookingDates;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonCodecTest {

    @Test(groups = "smoke")
    public void bookingRoundTripsThroughJackson() {
        Booking original = new Booking(
                "Jane",
                "Doe",
                150,
                true,
                new BookingDates("2026-12-01", "2026-12-04"),
                "Breakfast");
        String json = JsonCodec.instance().toJson(original);
        Booking restored = JsonCodec.instance().fromJson(json, Booking.class);
        Assert.assertEquals(restored, original);
        Assert.assertTrue(json.contains("\"firstname\":\"Jane\""));
        Assert.assertTrue(json.contains("\"bookingdates\""));
    }

    @Test(groups = "smoke")
    public void validBookingsFileDeserializes() {
        Booking[] bookings = JsonCodec.instance().fromResource(
                "testdata/booker/valid-bookings.json", Booking[].class);
        Assert.assertTrue(bookings.length >= 2);
        Assert.assertEquals(bookings[0].firstname(), "Alice");
        Assert.assertEquals(bookings[0].bookingdates().checkin(), "2026-12-01");
    }
}
