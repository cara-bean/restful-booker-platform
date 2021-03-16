package com.automationintesting.integration;

import com.automationintesting.api.BookingApplication;
import com.automationintesting.model.db.Booking;
import com.automationintesting.model.db.CreatedBooking;
import com.xebialabs.restito.server.StubServer;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Scanner;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.post;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;


// We need to start the app up to test it. So we use the SpringRunner class and SpringBootTest to configure
// and run the app.
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = BookingApplication.class)
@ActiveProfiles("dev")
public class CaraBookingIntegrationIT {

    private StubServer server;

    // We add the @Before annotation so that when JUnit runs it knows to run this method before
    // the tests are started. This is known as a hook.
    @Before
    // We give the before hook a clear name to ensure that it is descriptive in what it is checking
    public void setupRestito() {
        // Booking relies on the Message service so we will mock the message API. We do that by creating a
        // StubServer that we will later configure.
        server = new StubServer(3006).run();

        whenHttp(server).
                match(post("/message/")).
                then(status(HttpStatus.OK_200));
    }

    // Once the test is finished we need to stop the mock server
    @After
    // We give the after hook a clear name to ensure that it is description in what it's doing
    public void stopServer(){
        server.stop();
    }

    // We add the @Test annotation so that when JUnit runs it knows which
    // methods to run as tests
    @Test
    public void testBookingValid() {
        LocalDate checkindate = LocalDate.of(2021, Month.APRIL, 1);
        LocalDate checkoutdate = LocalDate.of(2021, Month.APRIL, 3);


        Booking bookingPayload = new Booking.BookingBuilder()
                .setRoomid(1)
                .setDepositpaid(true)
                .setFirstname("cara")
                .setLastname("bee")
                .setEmail("cara@test.com")
                .setPhone("01234123123")
                .setCheckin(checkindate)
                .setCheckout(checkoutdate)
                .build();

        Response bookingResponse = given()
                .contentType(ContentType.JSON)
                .body(bookingPayload)
                .when()
                .post("http://localhost:3000/booking/");

        System.out.println(bookingResponse.getBody().prettyPrint());

        // Once we get a response we extract the body and map it to CreatedBooking
        CreatedBooking response = bookingResponse.as(CreatedBooking.class);

        // Finally we assert on the various values we get from the HTTP response body
        assertEquals(bookingResponse.statusCode(), 201);
        assertThat(response.getBooking().getRoomid(), is(1));
        assertThat(response.getBooking().isDepositpaid(), is(true));
        assertThat(response.getBooking().getFirstname(), is("cara"));
        assertThat(response.getBooking().getLastname(), is("bee"));
        //assertThat(response.getBooking().getEmail(), is("cara@test.com"));
        //assertThat(response.getBooking().getPhone(), is("01234123123"));
        assertThat(response.getBooking().getBookingDates().getCheckin(), is(checkindate));
        assertThat(response.getBooking().getBookingDates().getCheckout(), is(checkoutdate));
    }

    @Test
    public void checkAuthContract() throws JSONException, FileNotFoundException, URISyntaxException {
        // First we make an HTTP request to get the Booking from Booking API
        Response response = given()
                .get("http://localhost:3000/booking/2");

        // Next we take the body of the HTTP response and convert it into a JSONObject
        JSONObject parsedResponse = new JSONObject(response.body().prettyPrint());

        // Then we import our expected JSON contract from the contract folder
        // and store in a string
        File file = ResourceUtils.getFile(this.getClass().getResource("/booking2.json"));
        String testObject = new Scanner(file).useDelimiter("\\Z").next();

        // Finally we compare the contract string and the JSONObject to compare
        // and pass if they match
        JSONAssert.assertEquals(testObject, parsedResponse, true);
    }
}
