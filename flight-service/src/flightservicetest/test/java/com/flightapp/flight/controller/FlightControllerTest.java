package com.flightapp.flight.controller;

import com.flightapp.flight.constants.FlightStatus;
import com.flightapp.flight.dto.FlightSearchRequest;
import com.flightapp.flight.dto.InventoryRequest;
import com.flightapp.flight.entity.FlightInventory;
import com.flightapp.flight.exception.FlightNotFoundException;
import com.flightapp.flight.service.FlightService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private FlightService flightService;

    private InventoryRequest inventoryRequest;
    private FlightInventory flightInventory;
    private FlightSearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        inventoryRequest = InventoryRequest.builder()
                .airline("Air India")
                .flightNumber("AI101")
                .fromPlace("Delhi")
                .toPlace("Mumbai")
                .departureDateTime(LocalDateTime.now().plusDays(1))
                .arrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .totalSeats(180)
                .ticketPrice(5000.0)
                .oneWayPrice(5000.0)
                .roundTripPrice(9000.0)
                .mealAvailable(true)
                .build();

        flightInventory = FlightInventory.builder()
                .id("flight123")
                .airline("Air India")
                .flightNumber("AI101")
                .fromPlace("Delhi")
                .toPlace("Mumbai")
                .departureDateTime(LocalDateTime.now().plusDays(1))
                .arrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .totalSeats(180)
                .availableSeats(180)
                .ticketPrice(5000.0)
                .oneWayPrice(5000.0)
                .roundTripPrice(9000.0)
                .mealAvailable(true)
                .flightStatus(FlightStatus.ACTIVE)
                .build();

        searchRequest = FlightSearchRequest.builder()
                .fromPlace("Delhi")
                .toPlace("Mumbai")
                .journeyDate(LocalDate.now().plusDays(1))
                .build();
    }

    @Test
    void testAddInventory_Success() {
        // Arrange
        when(flightService.addInventory(any(InventoryRequest.class))).thenReturn(Mono.just(flightInventory));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/flight/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FlightInventory.class)
                .value(response -> {
                    assert response.getFlightNumber().equals("AI101");
                    assert response.getFromPlace().equals("Delhi");
                    assert response.getToPlace().equals("Mumbai");
                });
    }

    @Test
    void testSearchFlights_Success() {
        // Arrange
        when(flightService.searchFlights(any(FlightSearchRequest.class)))
                .thenReturn(Flux.just(flightInventory, flightInventory));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/flight/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(searchRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FlightInventory.class)
                .hasSize(2);
    }

    @Test
    void testSearchFlights_NoResults() {
        // Arrange
        when(flightService.searchFlights(any(FlightSearchRequest.class)))
                .thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/flight/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(searchRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FlightInventory.class)
                .hasSize(0);
    }

    @Test
    void testGetFlightById_Success() {
        // Arrange
        when(flightService.getFlightById("flight123")).thenReturn(Mono.just(flightInventory));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/flight/flight123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(FlightInventory.class)
                .value(response -> {
                    assert response.getId().equals("flight123");
                    assert response.getFlightNumber().equals("AI101");
                });
    }

    @Test
    void testGetFlightById_NotFound() {
        // Arrange
        when(flightService.getFlightById("INVALID"))
                .thenReturn(Mono.error(new FlightNotFoundException("Flight not found")));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/flight/INVALID")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testUpdateSeats_Success() {
        // Arrange
        FlightInventory updatedFlight = FlightInventory.builder()
                .id("flight123")
                .airline("Air India")
                .flightNumber("AI101")
                .fromPlace("Delhi")
                .toPlace("Mumbai")
                .departureDateTime(LocalDateTime.now().plusDays(1))
                .arrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .totalSeats(180)
                .availableSeats(178)
                .ticketPrice(5000.0)
                .flightStatus(FlightStatus.ACTIVE)
                .build();

        when(flightService.updateAvailableSeats("flight123", 2)).thenReturn(Mono.just(updatedFlight));

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/flight/flight123/seats?seatsToReduce=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(FlightInventory.class)
                .value(response -> {
                    assert response.getId().equals("flight123");
                    assert response.getAvailableSeats().equals(178);
                });
    }

    @Test
    void testUpdateSeats_FlightNotFound() {
        // Arrange
        when(flightService.updateAvailableSeats("INVALID", 2))
                .thenReturn(Mono.error(new FlightNotFoundException("Flight not found")));

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/flight/INVALID/seats?seatsToReduce=2")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testUpdateSeats_InsufficientSeats() {
        // Arrange
        when(flightService.updateAvailableSeats("flight123", 200))
                .thenReturn(Mono.error(new IllegalArgumentException("Not enough available seats")));

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/flight/flight123/seats?seatsToReduce=200")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
