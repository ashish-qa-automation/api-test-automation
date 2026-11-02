package com.ashishqa.demo.booker.tests;

import com.ashishqa.api.base.BaseApiTest;
import com.ashishqa.api.dataproviders.JsonDataProvider;
import com.ashishqa.demo.booker.clients.BookingClient;
import com.ashishqa.demo.booker.models.Booking;
import com.ashishqa.demo.booker.models.BookingScenario;
import com.ashishqa.demo.booker.models.CreatedBooking;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Data-driven Restful Booker creates. Expected statuses encode the API's actual behaviour.
 */
public class BookingPayloadTest extends BaseApiTest {

    @DataProvider(name = "validBookings")
    public Object[][] validBookings() {
        return JsonDataProvider.array("testdata/booker/valid-bookings.json", Booking[].class);
    }

    @DataProvider(name = "boundaryBookings")
    public Object[][] boundaryBookings() {
        return JsonDataProvider.array("testdata/booker/boundary-bookings.json", BookingScenario[].class);
    }

    @DataProvider(name = "invalidBookings")
    public Object[][] invalidBookings() {
        return JsonDataProvider.array("testdata/booker/invalid-bookings.json", BookingScenario[].class);
    }

    @Test(groups = "smoke", dataProvider = "validBookings")
    public void validBookingIsCreatedAndRoundTrips(Booking template) {
        Booking expected = template.isolated();
        BookingClient client = new BookingClient(anonymousRestClient());
        CreatedBooking created = client.createBookingTyped(expected);
        Assert.assertTrue(created.bookingid() > 0, "bookingid should be assigned");
        Assert.assertEquals(created.booking().firstname(), expected.firstname());
        Assert.assertEquals(created.booking().lastname(), expected.lastname());
        Assert.assertEquals(created.booking().totalprice(), expected.totalprice());

        Booking fetched = client.getBookingTyped(created.bookingid());
        Assert.assertEquals(fetched.firstname(), expected.firstname());
        Assert.assertEquals(fetched.lastname(), expected.lastname());

        new BookingClient(restClient()).deleteBooking(created.bookingid());
    }

    @Test(groups = "smoke", dataProvider = "boundaryBookings")
    public void boundaryBookingMatchesDocumentedStatus(BookingScenario scenario) {
        postScenarioAndCleanup(scenario);
    }

    @Test(groups = "smoke", dataProvider = "invalidBookings")
    public void invalidBookingMatchesDocumentedStatus(BookingScenario scenario) {
        postScenarioAndCleanup(scenario);
    }

    private void postScenarioAndCleanup(BookingScenario scenario) {
        Response response = new BookingClient(anonymousRestClient()).createBookingPayload(scenario.payload());
        Assert.assertEquals(
                response.statusCode(),
                scenario.expectedStatus(),
                scenario.scenario() + " body=" + response.asString());
        if (response.statusCode() == 200 && response.jsonPath().get("bookingid") != null) {
            int id = response.jsonPath().getInt("bookingid");
            new BookingClient(restClient()).deleteBooking(id);
        }
    }
}
