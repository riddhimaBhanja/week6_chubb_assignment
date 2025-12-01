package com.flightapp.booking.controller;

import com.flightapp.booking.constants.BookingStatus;
import com.flightapp.booking.constants.MealType;
import com.flightapp.booking.dto.BookRequest;
import com.flightapp.booking.dto.BookingResponse;
import com.flightapp.booking.entity.Passenger;
import com.flightapp.booking.exception.BookingNotFoundException;
import com.flightapp.booking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookingService bookingService;

    private BookRequest bookRequest;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        Passenger passenger1 = Passenger.builder()
                .name("John Doe")
                .gender("Male")
                .age(30)
                .email("john@example.com")
                .build();

        Passenger passenger2 = Passenger.builder()
                .name("Jane Doe")
                .gender("Female")
                .age(28)
                .email("jane@example.com")
                .build();

        bookRequest = new BookRequest();
        bookRequest.setUserName("John Doe");
        bookRequest.setUserEmail("john@example.com");
        bookRequest.setNoOfSeats(2);
        bookRequest.setJourneyDate(LocalDate.now().plusDays(1));
        bookRequest.setMealType(MealType.VEG);
        bookRequest.setPassengers(List.of(passenger1, passenger2));

        bookingResponse = BookingResponse.builder()
                .pnr("PNR12345678")
                .flightId("flight123")
                .flightNumber("AI101")
                .airline("Air India")
                .fromPlace("Delhi")
                .toPlace("Mumbai")
                .departureDateTime(LocalDateTime.now().plusDays(1))
                .arrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .userName("John Doe")
                .userEmail("john@example.com")
                .journeyDate(LocalDate.now().plusDays(1))
                .noOfSeats(2)
                .mealType(MealType.VEG)
                .totalAmount(10000.0)
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingDateTime(LocalDateTime.now())
                .passengers(List.of(passenger1, passenger2))
                .build();
    }

    @Test
    void testBookTicket_Success() {
        // Arrange
        when(bookingService.bookTicket(eq("flight123"), any(BookRequest.class)))
                .thenReturn(Mono.just(bookingResponse));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/booking/book/flight123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookingResponse.class)
                .value(response -> {
                    assert response.getPnr().equals("PNR12345678");
                    assert response.getFlightNumber().equals("AI101");
                    assert response.getUserName().equals("John Doe");
                });
    }

    @Test
    void testGetBookingByPnr_Success() {
        // Arrange
        when(bookingService.getBookingByPnr("PNR12345678"))
                .thenReturn(Mono.just(bookingResponse));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/booking/PNR12345678")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookingResponse.class)
                .value(response -> {
                    assert response.getPnr().equals("PNR12345678");
                    assert response.getUserName().equals("John Doe");
                });
    }

    @Test
    void testGetBookingByPnr_NotFound() {
        // Arrange
        when(bookingService.getBookingByPnr("INVALID"))
                .thenReturn(Mono.error(new BookingNotFoundException("Booking not found")));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/booking/INVALID")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testGetBookingHistory_Success() {
        // Arrange
        when(bookingService.getBookingHistory("john@example.com"))
                .thenReturn(Flux.just(bookingResponse, bookingResponse));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/booking/history/john@example.com")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .hasSize(2);
    }

    @Test
    void testCancelBooking_Success() {
        // Arrange
        bookingResponse.setBookingStatus(BookingStatus.CANCELLED);
        when(bookingService.cancelBooking("PNR12345678"))
                .thenReturn(Mono.just(bookingResponse));

        // Act & Assert
        webTestClient.delete()
                .uri("/api/v1/booking/cancel/PNR12345678")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookingResponse.class)
                .value(response -> {
                    assert response.getPnr().equals("PNR12345678");
                    assert response.getBookingStatus().equals(BookingStatus.CANCELLED);
                });
    }

    @Test
    void testCancelBooking_NotFound() {
        // Arrange
        when(bookingService.cancelBooking("INVALID"))
                .thenReturn(Mono.error(new BookingNotFoundException("Booking not found")));

        // Act & Assert
        webTestClient.delete()
                .uri("/api/v1/booking/cancel/INVALID")
                .exchange()
                .expectStatus().isNotFound();
    }
}
