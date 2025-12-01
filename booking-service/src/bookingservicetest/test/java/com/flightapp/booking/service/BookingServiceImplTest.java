package com.flightapp.booking.service;

import com.flightapp.booking.client.FlightServiceWebClient;
import com.flightapp.booking.constants.BookingStatus;
import com.flightapp.booking.constants.MealType;
import com.flightapp.booking.dto.BookRequest;
import com.flightapp.booking.dto.BookingResponse;
import com.flightapp.booking.dto.FlightDto;
import com.flightapp.booking.entity.Booking;
import com.flightapp.booking.event.BookingEvent;
import com.flightapp.booking.exception.BookingNotFoundException;
import com.flightapp.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightServiceWebClient flightServiceClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private FlightDto flightDto;
    private BookRequest bookRequest;
    private Booking booking;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bookingService, "exchange", "booking-exchange");
        ReflectionTestUtils.setField(bookingService, "routingKey", "booking-routing-key");

        // Setup Flight DTO
        flightDto = FlightDto.builder()
                .id("flight123")
                .flightNumber("AI101")
                .airline("Air India")
                .fromPlace("Delhi")
                .toPlace("Mumbai")
                .departureDateTime(LocalDateTime.now().plusDays(1))
                .arrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .availableSeats(50)
                .ticketPrice(5000.0)
                .build();

        // Setup Book Request
        bookRequest = new BookRequest();
        bookRequest.setUserName("John Doe");
        bookRequest.setUserEmail("john@example.com");
        bookRequest.setNoOfSeats(2);
        bookRequest.setJourneyDate(LocalDateTime.now().plusDays(1).toLocalDate());
        bookRequest.setMealType(MealType.VEG);
        bookRequest.setPassengers(new ArrayList<>());

        // Setup Booking
        booking = Booking.builder()
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
                .journeyDate(LocalDateTime.now().plusDays(1).toLocalDate())
                .noOfSeats(2)
                .mealType(MealType.VEG)
                .totalAmount(10000.0)
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingDateTime(LocalDateTime.now())
                .passengers(new ArrayList<>())
                .build();
    }

    @Test
    void testBookTicket_Success() {
        // Arrange
        when(flightServiceClient.getFlightById("flight123")).thenReturn(Mono.just(flightDto));
        when(flightServiceClient.updateSeats("flight123", 2)).thenReturn(Mono.just(flightDto));
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(booking));

        // Act
        Mono<BookingResponse> result = bookingService.bookTicket("flight123", bookRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("AI101", response.getFlightNumber());
                    assertEquals("John Doe", response.getUserName());
                    assertEquals(BookingStatus.CONFIRMED, response.getBookingStatus());
                })
                .verifyComplete();

        verify(flightServiceClient).getFlightById("flight123");
        verify(flightServiceClient).updateSeats("flight123", 2);
        verify(bookingRepository).save(any(Booking.class));
        verify(rabbitTemplate).convertAndSend(eq("booking-exchange"), eq("booking-routing-key"), any(BookingEvent.class));
    }

    @Test
    void testBookTicket_InsufficientSeats() {
        // Arrange
        flightDto.setAvailableSeats(1);
        when(flightServiceClient.getFlightById("flight123")).thenReturn(Mono.just(flightDto));

        // Act
        Mono<BookingResponse> result = bookingService.bookTicket("flight123", bookRequest);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Not enough seats available"))
                .verify();

        verify(flightServiceClient).getFlightById("flight123");
        verify(flightServiceClient, never()).updateSeats(anyString(), anyInt());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testBookTicket_FlightNotFound() {
        // Arrange
        when(flightServiceClient.getFlightById("flight123"))
                .thenReturn(Mono.error(new RuntimeException("Flight not found")));

        // Act
        Mono<BookingResponse> result = bookingService.bookTicket("flight123", bookRequest);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(flightServiceClient).getFlightById("flight123");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testGetBookingByPnr_Success() {
        // Arrange
        when(bookingRepository.findByPnr("PNR12345678")).thenReturn(Mono.just(booking));

        // Act
        Mono<BookingResponse> result = bookingService.getBookingByPnr("PNR12345678");

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("PNR12345678", response.getPnr());
                    assertEquals("John Doe", response.getUserName());
                })
                .verifyComplete();

        verify(bookingRepository).findByPnr("PNR12345678");
    }

    @Test
    void testGetBookingByPnr_NotFound() {
        // Arrange
        when(bookingRepository.findByPnr("INVALID")).thenReturn(Mono.empty());

        // Act
        Mono<BookingResponse> result = bookingService.getBookingByPnr("INVALID");

        // Assert
        StepVerifier.create(result)
                .expectError(BookingNotFoundException.class)
                .verify();

        verify(bookingRepository).findByPnr("INVALID");
    }

    @Test
    void testGetBookingHistory_Success() {
        // Arrange
        when(bookingRepository.findByUserEmail("john@example.com"))
                .thenReturn(Flux.just(booking, booking));

        // Act
        Flux<BookingResponse> result = bookingService.getBookingHistory("john@example.com");

        // Assert
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

        verify(bookingRepository).findByUserEmail("john@example.com");
    }

    @Test
    void testCancelBooking_Success() {
        // Arrange
        when(bookingRepository.findByPnr("PNR12345678")).thenReturn(Mono.just(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(booking));

        // Act
        Mono<BookingResponse> result = bookingService.cancelBooking("PNR12345678");

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    // Status will be updated to CANCELLED in the actual save
                })
                .verifyComplete();

        verify(bookingRepository).findByPnr("PNR12345678");
        verify(bookingRepository).save(any(Booking.class));
        verify(rabbitTemplate).convertAndSend(eq("booking-exchange"), eq("booking-routing-key"), any(BookingEvent.class));
    }

    @Test
    void testCancelBooking_AlreadyCancelled() {
        // Arrange
        booking.setBookingStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findByPnr("PNR12345678")).thenReturn(Mono.just(booking));

        // Act
        Mono<BookingResponse> result = bookingService.cancelBooking("PNR12345678");

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("already cancelled"))
                .verify();

        verify(bookingRepository).findByPnr("PNR12345678");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testCancelBooking_NotFound() {
        // Arrange
        when(bookingRepository.findByPnr("INVALID")).thenReturn(Mono.empty());

        // Act
        Mono<BookingResponse> result = bookingService.cancelBooking("INVALID");

        // Assert
        StepVerifier.create(result)
                .expectError(BookingNotFoundException.class)
                .verify();

        verify(bookingRepository).findByPnr("INVALID");
        verify(bookingRepository, never()).save(any());
    }
}
