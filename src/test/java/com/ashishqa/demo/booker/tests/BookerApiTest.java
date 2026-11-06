package com.ashishqa.demo.booker.tests;

import com.ashishqa.api.base.BaseApiTest;
import com.ashishqa.demo.booker.assertions.BookingAssert;
import com.ashishqa.demo.booker.clients.BookingClient;
import com.ashishqa.demo.booker.models.Booking;
import com.ashishqa.demo.booker.models.CreatedBooking;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Restful Booker tests that create their own bookings and delete them afterwards.
 * Ids stay on the test thread so a later parallel run cannot clean up another test's data.
 *
 * <p>Failure classification when a test does not pass:
 * <ul>
 *   <li>{@code AssertionError} — the API responded and the contract did not match.</li>
 *   <li>{@code IllegalStateException} from {@code RestClient} — no response (API down, timeout, or a client bug).</li>
 *   <li>{@code AuthenticationException} — {@code POST /auth} did not return a usable cookie token.</li>
 * </ul>
 */
public abstract class BookerApiTest extends BaseApiTest {

    private final ThreadLocal<List<Integer>> createdIds = ThreadLocal.withInitial(ArrayList::new);

    protected BookingClient publicBookings() {
        return new BookingClient(anonymousRestClient());
    }

    protected BookingClient authenticatedBookings() {
        return new BookingClient(restClient());
    }

    protected CreatedBooking createTracked(Booking booking) {
        Response response = publicBookings().createBooking(booking);
        BookingAssert.createdMatchesSchema(response);
        CreatedBooking created = response.as(CreatedBooking.class);
        createdIds.get().add(created.bookingid());
        return created;
    }

    protected void untrack(int bookingId) {
        createdIds.get().remove(Integer.valueOf(bookingId));
    }

    @AfterMethod(alwaysRun = true)
    protected void deleteTrackedBookings() {
        List<Integer> ids = createdIds.get();
        try {
            if (!ids.isEmpty()) {
                BookingClient client = authenticatedBookings();
                for (int id : List.copyOf(ids)) {
                    client.deleteBooking(id);
                }
            }
        } finally {
            createdIds.remove();
        }
    }
}
