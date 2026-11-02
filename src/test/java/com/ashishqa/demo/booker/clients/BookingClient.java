package com.ashishqa.demo.booker.clients;

import com.ashishqa.api.clients.ApiRequest;
import com.ashishqa.api.clients.RestClient;
import com.ashishqa.demo.booker.models.Booking;
import com.ashishqa.demo.booker.models.CreatedBooking;
import io.restassured.response.Response;

/**
 * Restful Booker endpoints. Create is public; update and delete need a cookie token on the RestClient.
 */
public final class BookingClient {

    public static final String PING = "/ping";
    public static final String BOOKINGS = "/booking";
    public static final String BOOKING_BY_ID = "/booking/{id}";

    private final RestClient restClient;

    public BookingClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public Response ping() {
        return restClient.get(ApiRequest.to(PING).anonymous());
    }

    public Response getBookingIds() {
        return restClient.get(ApiRequest.to(BOOKINGS).anonymous());
    }

    public Response getBooking(int id) {
        return restClient.get(ApiRequest.to(BOOKING_BY_ID).anonymous().pathParam("id", id));
    }

    public Response createBooking(Booking booking) {
        return createBookingPayload(booking);
    }

    public Response createBookingPayload(Object body) {
        return restClient.post(ApiRequest.to(BOOKINGS).anonymous().body(body));
    }

    public CreatedBooking createBookingTyped(Booking booking) {
        return createBooking(booking).as(CreatedBooking.class);
    }

    public Response updateBooking(int id, Booking booking) {
        return restClient.put(ApiRequest.to(BOOKING_BY_ID).pathParam("id", id).body(booking));
    }

    public Booking getBookingTyped(int id) {
        return getBooking(id).as(Booking.class);
    }

    public Response deleteBooking(int id) {
        return restClient.delete(ApiRequest.to(BOOKING_BY_ID).pathParam("id", id));
    }

    public Response deleteBookingAnonymously(int id) {
        return restClient.delete(ApiRequest.to(BOOKING_BY_ID).anonymous().pathParam("id", id));
    }
}
