package com.ashishqa.demo.booker.clients;

import com.ashishqa.api.clients.ApiRequest;
import com.ashishqa.api.clients.RestClient;
import com.ashishqa.api.config.FrameworkConfig;
import io.restassured.response.Response;

/**
 * Restful Booker endpoints. CRUD and auth calls expand in later phases.
 */
public final class BookingClient {

    public static final String PING = "/ping";
    public static final String BOOKINGS = "/booking";
    public static final String BOOKING_BY_ID = "/booking/{id}";

    private final RestClient restClient;

    public BookingClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public BookingClient(FrameworkConfig config) {
        this(new RestClient(config));
    }

    public Response ping() {
        return restClient.get(PING);
    }

    public Response getBookingIds() {
        return restClient.get(BOOKINGS);
    }

    public Response getBooking(int id) {
        return restClient.get(ApiRequest.to(BOOKING_BY_ID).pathParam("id", id));
    }
}
