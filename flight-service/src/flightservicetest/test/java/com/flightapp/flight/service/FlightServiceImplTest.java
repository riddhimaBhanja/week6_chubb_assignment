package com.flightapp.flight.service;

import com.flightapp.flight.constants.FlightStatus;
import com.flightapp.flight.dto.FlightSearchRequest;
import com.flightapp.flight.dto.InventoryRequest;
import com.flightapp.flight.entity.FlightInventory;
import com.flightapp.flight.exception.FlightNotFoundException;
import com.flightapp.flight.repository.FlightInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceImplTest {

    @Mock
    private FlightInventoryRepository flightInventoryRepository;

    @InjectMocks
    private FlightServiceImpl flightService;

    private InventoryRequest inventoryRequest;
    private FlightInventory flightInventory;
    private FlightSearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        // Setup Inventory Request
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

        // Setup Flight Inventory
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

        // Setup Search Request
        searchRequest = FlightSearchRequest.builder()
                .fromPlace("Delhi")
                .toPlace("Mumbai")
                .journeyDate(LocalDate.now().plusDays(1))
                .build();
    }

    @Test
    void testAddInventory_Success() {
        // Arrange
        when(flightInventoryRepository.save(any(FlightInventory.class))).thenReturn(Mono.just(flightInventory));

        // Act
        Mono<FlightInventory> result = flightService.addInventory(inventoryRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(flight -> {
                    assertNotNull(flight);
                    assertEquals("AI101", flight.getFlightNumber());
                    assertEquals("Delhi", flight.getFromPlace());
                    assertEquals("Mumbai", flight.getToPlace());
                    assertEquals(180, flight.getTotalSeats());
                    assertEquals(180, flight.getAvailableSeats());
                    assertEquals(FlightStatus.ACTIVE, flight.getFlightStatus());
                })
                .verifyComplete();

        verify(flightInventoryRepository).save(any(FlightInventory.class));
    }

    @Test
    void testSearchFlights_Success() {
        // Arrange
        LocalDateTime startOfDay = searchRequest.getJourneyDate().atStartOfDay();
        LocalDateTime endOfDay = searchRequest.getJourneyDate().atTime(23, 59, 59, 999999999);

        when(flightInventoryRepository.findByFromPlaceAndToPlaceAndDepartureDateTimeBetween(
                eq("Delhi"), eq("Mumbai"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Flux.just(flightInventory));

        // Act
        Flux<FlightInventory> result = flightService.searchFlights(searchRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(flight -> {
                    assertNotNull(flight);
                    assertEquals("AI101", flight.getFlightNumber());
                    assertEquals("Delhi", flight.getFromPlace());
                    assertEquals("Mumbai", flight.getToPlace());
                    assertTrue(flight.getAvailableSeats() > 0);
                    assertEquals(FlightStatus.ACTIVE, flight.getFlightStatus());
                })
                .verifyComplete();

        verify(flightInventoryRepository).findByFromPlaceAndToPlaceAndDepartureDateTimeBetween(
                eq("Delhi"), eq("Mumbai"), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testSearchFlights_FiltersInactiveFlights() {
        // Arrange
        FlightInventory inactiveFlight = FlightInventory.builder()
                .id("flight456")
                .airline("Air India")
                .flightNumber("AI102")
                .fromPlace("Delhi")
                .toPlace("Mumbai")
                .departureDateTime(LocalDateTime.now().plusDays(1))
                .arrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .totalSeats(180)
                .availableSeats(100)
                .flightStatus(FlightStatus.CANCELLED)
                .build();

        when(flightInventoryRepository.findByFromPlaceAndToPlaceAndDepartureDateTimeBetween(
                eq("Delhi"), eq("Mumbai"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Flux.just(flightInventory, inactiveFlight));

        // Act
        Flux<FlightInventory> result = flightService.searchFlights(searchRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(1) // Only active flight should be returned
                .verifyComplete();
    }

    @Test
    void testSearchFlights_FiltersFullFlights() {
        // Arrange
        FlightInventory fullFlight = FlightInventory.builder()
                .id("flight456")
                .airline("Air India")
                .flightNumber("AI102")
                .fromPlace("Delhi")
                .toPlace("Mumbai")
                .departureDateTime(LocalDateTime.now().plusDays(1))
                .arrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .totalSeats(180)
                .availableSeats(0) // No available seats
                .flightStatus(FlightStatus.ACTIVE)
                .build();

        when(flightInventoryRepository.findByFromPlaceAndToPlaceAndDepartureDateTimeBetween(
                eq("Delhi"), eq("Mumbai"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Flux.just(flightInventory, fullFlight));

        // Act
        Flux<FlightInventory> result = flightService.searchFlights(searchRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(1) // Only flight with available seats should be returned
                .verifyComplete();
    }

    @Test
    void testGetFlightById_Success() {
        // Arrange
        when(flightInventoryRepository.findById("flight123")).thenReturn(Mono.just(flightInventory));

        // Act
        Mono<FlightInventory> result = flightService.getFlightById("flight123");

        // Assert
        StepVerifier.create(result)
                .assertNext(flight -> {
                    assertNotNull(flight);
                    assertEquals("flight123", flight.getId());
                    assertEquals("AI101", flight.getFlightNumber());
                })
                .verifyComplete();

        verify(flightInventoryRepository).findById("flight123");
    }

    @Test
    void testGetFlightById_NotFound() {
        // Arrange
        when(flightInventoryRepository.findById("INVALID")).thenReturn(Mono.empty());

        // Act
        Mono<FlightInventory> result = flightService.getFlightById("INVALID");

        // Assert
        StepVerifier.create(result)
                .expectError(FlightNotFoundException.class)
                .verify();

        verify(flightInventoryRepository).findById("INVALID");
    }

    @Test
    void testUpdateAvailableSeats_Success() {
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
                .availableSeats(178) // Reduced by 2
                .ticketPrice(5000.0)
                .flightStatus(FlightStatus.ACTIVE)
                .build();

        when(flightInventoryRepository.findById("flight123")).thenReturn(Mono.just(flightInventory));
        when(flightInventoryRepository.save(any(FlightInventory.class))).thenReturn(Mono.just(updatedFlight));

        // Act
        Mono<FlightInventory> result = flightService.updateAvailableSeats("flight123", 2);

        // Assert
        StepVerifier.create(result)
                .assertNext(flight -> {
                    assertNotNull(flight);
                    assertEquals(178, flight.getAvailableSeats());
                })
                .verifyComplete();

        verify(flightInventoryRepository).findById("flight123");
        verify(flightInventoryRepository).save(any(FlightInventory.class));
    }

    @Test
    void testUpdateAvailableSeats_InsufficientSeats() {
        // Arrange
        when(flightInventoryRepository.findById("flight123")).thenReturn(Mono.just(flightInventory));

        // Act
        Mono<FlightInventory> result = flightService.updateAvailableSeats("flight123", 200);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Not enough available seats"))
                .verify();

        verify(flightInventoryRepository).findById("flight123");
        verify(flightInventoryRepository, never()).save(any());
    }

    @Test
    void testUpdateAvailableSeats_FlightNotFound() {
        // Arrange
        when(flightInventoryRepository.findById("INVALID")).thenReturn(Mono.empty());

        // Act
        Mono<FlightInventory> result = flightService.updateAvailableSeats("INVALID", 2);

        // Assert
        StepVerifier.create(result)
                .expectError(FlightNotFoundException.class)
                .verify();

        verify(flightInventoryRepository).findById("INVALID");
        verify(flightInventoryRepository, never()).save(any());
    }
}
