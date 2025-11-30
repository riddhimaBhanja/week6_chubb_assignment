package com.flightapp.service.impl;

import com.flightapp.dto.BookRequest;
import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.dto.InventoryRequest;
import com.flightapp.entity.Booking;
import com.flightapp.entity.FlightInventory;
import com.flightapp.exception.FlightNotFoundException;
import com.flightapp.exception.PNRNotFoundException;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightInventoryRepository;
import constants.MealType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class FlightServiceImplTest {

    @Mock
    private FlightInventoryRepository flightInventoryRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private FlightServiceImpl flightService;

    @Captor
    private ArgumentCaptor<FlightInventory> flightCaptor;

    @Captor
    private ArgumentCaptor<Booking> bookingCaptor;

    @Test
    void addInventory_savesAndReturnsFlight() {
        InventoryRequest request = InventoryRequest.builder()
                .flightNumber("AI101")
                .airline("AirTest")
                .fromPlace("DEL")
                .toPlace("BOM")
                .departureDateTime(LocalDateTime.of(2025, 12, 1, 10, 0))
                .totalSeats(120)
                .ticketPrice(5000.0)
                .build();

        FlightInventory saved = FlightInventory.builder()
                .id("id-1")
                .flightNumber("AI101")
                .airline("AirTest")
                .fromPlace("DEL")
                .toPlace("BOM")
                .departureDateTime(request.getDepartureDateTime())
                .totalSeats(120)
                .ticketPrice(5000.0)
                .build();

        when(flightInventoryRepository.save(any(FlightInventory.class)))
                .thenReturn(Mono.just(saved));

        StepVerifier.create(flightService.addInventory(request))
                .assertNext(f -> {
                    assertThat(f.getId()).isEqualTo("id-1");
                    assertThat(f.getFlightNumber()).isEqualTo("AI101");
                    assertThat(f.getAirline()).isEqualTo("AirTest");
                })
                .verifyComplete();

        verify(flightInventoryRepository).save(flightCaptor.capture());
        FlightInventory passed = flightCaptor.getValue();
        assertThat(passed.getFlightNumber()).isEqualTo("AI101");
        assertThat(passed.getTotalSeats()).isEqualTo(120);
    }

    @Test
    void searchFlights_returnsFluxFromRepository() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .fromPlace("DEL")
                .toPlace("BOM")
                .journeyDate(LocalDate.of(2025, 12, 1))
                .build();

        FlightInventory f1 = FlightInventory.builder().id("f1").flightNumber("AI101").build();
        FlightInventory f2 = FlightInventory.builder().id("f2").flightNumber("AI102").build();

        when(flightInventoryRepository.findFlightsForDay(
                eq("DEL"),
                eq("BOM"),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(Flux.just(f1, f2));

        StepVerifier.create(flightService.searchFlights(request))
                .expectNextMatches(fi -> fi.getId().equals("f1"))
                .expectNextMatches(fi -> fi.getId().equals("f2"))
                .verifyComplete();
    }

    @Test
    void bookTicket_successfulBookingReturnsResponse() {
        String flightId = "f1";
        FlightInventory flight = FlightInventory.builder()
                .id(flightId)
                .flightNumber("AI101")
                .build();

        BookRequest req = BookRequest.builder()
                .passengerName("John Doe")
                .userEmail("john@test.com")
                .journeyDate(LocalDate.of(2025, 12, 1))
                .noOfSeats(2)
                .mealType(MealType.VEG)
                .build();

        Booking savedBooking = Booking.builder()
                .id("PNR123")
                .pnr("PNR123")
                .flightId(flightId)
                .userName("John Doe")
                .userEmail("john@test.com")
                .journeyDate(req.getJourneyDate())
                .noOfSeats(2)
                .mealType(MealType.VEG)
                .bookingStatus(constants.BookingStatus.CONFIRMED)
                .bookingDateTime(LocalDateTime.now())
                .build();

        when(flightInventoryRepository.findById(flightId)).thenReturn(Mono.just(flight));
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(savedBooking));

        StepVerifier.create(flightService.bookTicket(flightId, req))
                .assertNext(resp -> {
                    assertThat(resp.getPnr()).isEqualTo("PNR123");
                    assertThat(resp.getUserEmail()).isEqualTo("john@test.com");
                })
                .verifyComplete();

        verify(bookingRepository).save(bookingCaptor.capture());
        Booking toSave = bookingCaptor.getValue();
        assertThat(toSave.getFlightId()).isEqualTo(flightId);
        assertThat(toSave.getUserName()).isEqualTo("John Doe");
    }

    @Test
    void bookTicket_whenFlightNotFound_emitsError() {
        when(flightInventoryRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(flightService.bookTicket("nope", BookRequest.builder().build()))
                .expectErrorSatisfies(err -> assertThat(err).isInstanceOf(FlightNotFoundException.class))
                .verify();
    }

    @Test
    void getTicketByPnr_successAndNotFound() {
        Booking booking = Booking.builder()
                .pnr("PNR1")
                .userEmail("a@b.com")
                .userName("A")
                .build();

        when(bookingRepository.findByPnr("PNR1")).thenReturn(Mono.just(booking));
        when(bookingRepository.findByPnr("NOPE")).thenReturn(Mono.empty());

        StepVerifier.create(flightService.getTicketByPnr("PNR1"))
                .assertNext(resp -> assertThat(resp.getPnr()).isEqualTo("PNR1"))
                .verifyComplete();

        StepVerifier.create(flightService.getTicketByPnr("NOPE"))
                .expectErrorSatisfies(err -> assertThat(err).isInstanceOf(PNRNotFoundException.class))
                .verify();
    }

    @Test
    void getBookingHistory_returnsAllBookingsForEmail() {
        Booking b1 = Booking.builder().pnr("P1").userEmail("x@y.com").build();
        Booking b2 = Booking.builder().pnr("P2").userEmail("x@y.com").build();

        when(bookingRepository.findByUserEmail("x@y.com")).thenReturn(Flux.just(b1, b2));

        StepVerifier.create(flightService.getBookingHistory("x@y.com"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void cancelTicket_changesStatusAndReturnsUpdatedBooking() {
        Booking existing = Booking.builder()
                .pnr("PX")
                .bookingStatus(constants.BookingStatus.CONFIRMED)
                .build();

        Booking updated = Booking.builder()
                .pnr("PX")
                .bookingStatus(constants.BookingStatus.CANCELLED)
                .build();

        when(bookingRepository.findByPnr("PX")).thenReturn(Mono.just(existing));
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(flightService.cancelTicket("PX"))
                .assertNext(resp -> assertThat(resp.getBookingStatus()).isEqualTo(constants.BookingStatus.CANCELLED))
                .verifyComplete();

        verify(bookingRepository).save(bookingCaptor.capture());
        Booking saved = bookingCaptor.getValue();
        assertThat(saved.getBookingStatus()).isEqualTo(constants.BookingStatus.CANCELLED);
    }
}
