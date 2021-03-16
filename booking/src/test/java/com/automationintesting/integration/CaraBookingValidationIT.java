package com.automationintesting.integration;

import com.automationintesting.api.BookingApplication;
import com.automationintesting.model.db.Booking;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.Month;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = BookingApplication.class)
@ActiveProfiles("dev")
public class CaraBookingValidationIT {

    @Test
    public void testPostValidation() {
        Booking bookingPayload = new Booking.BookingBuilder()
                                        .setEmail("cara@test.com")
                                        .setPhone("01234123123")
                                        .build();

        Response response = given()
            .contentType(ContentType.JSON)
            .body(bookingPayload)
            .when()
            .post("http://localhost:3000/booking/");

        assertEquals(response.statusCode(), 400);
    }

    @Test
    public void testPutValidation() {
        Booking bookingPayload = new Booking.BookingBuilder()
                .setEmail("cara@test.com")
                .setPhone("01234123123")
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(bookingPayload)
                .when()
                .put("http://localhost:3000/booking/1");

        assertEquals(response.statusCode(), 400);
    }


    @Test
    public void testBookingNameTooShort() {
        LocalDate checkindate = LocalDate.of(2021, Month.APRIL, 1);
        LocalDate checkoutdate = LocalDate.of(2021, Month.APRIL, 3);


        Booking bookingPayload = new Booking.BookingBuilder()
                .setRoomid(1)
                .setDepositpaid(false)
                // min = 3
                .setFirstname("tom")
                .setLastname("li")
                .setCheckin(checkindate)
                .setCheckout(checkoutdate)
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(bookingPayload)
                .when()
                .post("http://localhost:3000/booking/");

        assertEquals(response.statusCode(), 400);
    }

    @Test
    public void testBookingNameTooLong() {
        LocalDate checkindate = LocalDate.of(2021, Month.APRIL, 1);
        LocalDate checkoutdate = LocalDate.of(2021, Month.APRIL, 3);


        Booking bookingPayload = new Booking.BookingBuilder()
                .setRoomid(1)
                .setDepositpaid(false)
                // max = 18 & 30
                .setFirstname("Keihanaikukauakahihuli")
                .setLastname("Keihanaikukauakahihuliheekahaunaele")
                .setCheckin(checkindate)
                .setCheckout(checkoutdate)
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(bookingPayload)
                .when()
                .post("http://localhost:3000/booking/");

        assertEquals(response.statusCode(), 400);
    }

    @Test
    public void testBookingRoomInvalid() {
        LocalDate checkindate = LocalDate.of(2021, Month.APRIL, 1);
        LocalDate checkoutdate = LocalDate.of(2021, Month.APRIL, 3);


        Booking bookingPayload = new Booking.BookingBuilder()
                // should not be possible - room 99 doesnt exist
                .setRoomid(99)
                .setDepositpaid(true)
                .setFirstname("cara")
                .setLastname("bee")
                //
                .setCheckin(checkindate)
                .setCheckout(checkoutdate)
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(bookingPayload)
                .when()
                .post("http://localhost:3000/booking/");

        assertEquals(response.statusCode(), 400);
    }

    @Test
    public void testBookingPhoneTooShort() {
        LocalDate checkindate = LocalDate.of(2021, Month.APRIL, 1);
        LocalDate checkoutdate = LocalDate.of(2021, Month.APRIL, 3);


        Booking bookingPayload = new Booking.BookingBuilder()
                .setRoomid(1)
                .setDepositpaid(false)
                .setFirstname("cara")
                .setLastname("bee")

                // min 11
                .setPhone("8022088080")
                .setCheckin(checkindate)
                .setCheckout(checkoutdate)
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(bookingPayload)
                .when()
                .post("http://localhost:3000/booking/");

        assertEquals(response.statusCode(), 400);
    }

    @Test
    public void testBookingEmailEmpty() {
        LocalDate checkindate = LocalDate.of(2021, Month.APRIL, 1);
        LocalDate checkoutdate = LocalDate.of(2021, Month.APRIL, 3);


        Booking bookingPayload = new Booking.BookingBuilder()
                // should not be possible - room 99 doesnt exist
                .setRoomid(99)
                .setDepositpaid(false)
                .setFirstname("cara")
                .setLastname("bee")
                //empty
                .setEmail("")
                .setCheckin(checkindate)
                .setCheckout(checkoutdate)
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(bookingPayload)
                .when()
                .post("http://localhost:3000/booking/");

        assertEquals(response.statusCode(), 400);
    }

    //this test isnt actually working
    // need to stub messaging api to get a response back from booking service
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

        Response response = given()
                .contentType(ContentType.JSON)
                .body(bookingPayload)
                .when()
                .post("http://localhost:3000/booking/");

        assertEquals(response.statusCode(), 201);
    }

}
