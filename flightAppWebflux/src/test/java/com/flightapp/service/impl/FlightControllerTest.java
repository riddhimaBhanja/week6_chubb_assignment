package com.flightapp.service.impl;


import com.flightapp.controller.FlightController;
import com.flightapp.dto.*;
import com.flightapp.entity.FlightInventory;
import com.flightapp.dto.BookingResponse;
import com.flightapp.service.FlightService;
import constants.MealType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class FlightControllerTest {

    @Mock
    private FlightService flightService;

    private WebTestClient webClient;

    @BeforeEach
    void setup() {
        FlightController controller = new FlightController(flightService);
        webClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void addInventory_returnsSavedInventory() {
        InventoryRequest req = InventoryRequest.builder()
                .flightNumber("AI101")
                .airline("AirTest")
                .fromPlace("DEL")
                .toPlace("BOM")
                .departureDateTime(LocalDateTime.of(2025, 12, 1, 10, 0))
                .totalSeats(100)
                .ticketPrice(2500.0)
                .build();

        FlightInventory saved = FlightInventory.builder()
                .id("id-1")
                .flightNumber("AI101")
                .airline("AirTest")
                .fromPlace("DEL")
                .toPlace("BOM")
                .departureDateTime(req.getDepartureDateTime())
                .totalSeats(100)
                .ticketPrice(2500.0)
                .build();

        when(flightService.addInventory(any(InventoryRequest.class))).thenReturn(Mono.just(saved));

        webClient.post()
                .uri("/api/v1/flight/airline/inventory")
                .bodyValue(req)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo("id-1")
                .jsonPath("$.flightNumber").isEqualTo("AI101")
                .jsonPath("$.airline").isEqualTo("AirTest");
    }

    @Test
    void searchFlights_returnsFluxOfInventories() {
        FlightSearchRequest req = FlightSearchRequest.builder()
                .fromPlace("DEL")
                .toPlace("BOM")
                .journeyDate(LocalDate.of(2025, 12, 1))
                .build();

        FlightInventory f1 = FlightInventory.builder().id("f1").flightNumber("AI101").build();
        FlightInventory f2 = FlightInventory.builder().id("f2").flightNumber("AI102").build();

        when(flightService.searchFlights(any(FlightSearchRequest.class))).thenReturn(Flux.just(f1, f2));

        webClient.post()
                .uri("/api/v1/flight/search")
                .bodyValue(req)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("f1")
                .jsonPath("$[1].id").isEqualTo("f2");
    }

    @Test
    void bookTicket_returnsBookingResponse() {
        String flightId = "f1";
        BookRequest req = BookRequest.builder()
                .passengerName("John")
                .userEmail("john@test.com")
                .journeyDate(LocalDate.of(2025, 12, 1))
                .noOfSeats(1)
                .mealType(MealType.VEG)
                .build();

        BookingResponse resp = BookingResponse.builder()
                .pnr("PNR123")
                .flightId(flightId)
                .userEmail("john@test.com")
                .userName("John")
                .noOfSeats(1)
                .build();

        when(flightService.bookTicket(eq(flightId), any(BookRequest.class))).thenReturn(Mono.just(resp));

        webClient.post()
                .uri("/api/v1/flight/booking/{flightId}", flightId)
                .bodyValue(req)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.pnr").isEqualTo("PNR123")
                .jsonPath("$.flightId").isEqualTo(flightId);
    }

    @Test
    void getTicket_returnsBookingResponse() {
        String pnr = "PNR999";
        BookingResponse resp = BookingResponse.builder()
                .pnr(pnr)
                .userEmail("x@y.com")
                .userName("X")
                .build();

        when(flightService.getTicketByPnr(pnr)).thenReturn(Mono.just(resp));

        webClient.get()
                .uri("/api/v1/flight/ticket/{pnr}", pnr)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.pnr").isEqualTo(pnr)
                .jsonPath("$.userEmail").isEqualTo("x@y.com");
    }

    @Test
    void history_returnsBookingHistoryFlux() {
        String email = "hist@t.com";
        BookingResponse b1 = BookingResponse.builder().pnr("P1").userEmail(email).build();
        BookingResponse b2 = BookingResponse.builder().pnr("P2").userEmail(email).build();

        when(flightService.getBookingHistory(email)).thenReturn(Flux.just(b1, b2));

        webClient.get()
                .uri("/api/v1/flight/booking/history/{emailId}", email)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$[0].pnr").isEqualTo("P1")
                .jsonPath("$[1].pnr").isEqualTo("P2");
    }

    @Test
    void cancelTicket_returnsCancelledBooking() {
        String pnr = "PX";
        BookingResponse resp = BookingResponse.builder()
                .pnr(pnr)
                .bookingStatus(constants.BookingStatus.CANCELLED)
                .build();

        when(flightService.cancelTicket(pnr)).thenReturn(Mono.just(resp));

        webClient.delete()
                .uri("/api/v1/flight/cancel/{pnr}", pnr)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.pnr").isEqualTo(pnr)
                .jsonPath("$.bookingStatus").isEqualTo(constants.BookingStatus.CANCELLED.name());
    }
}

