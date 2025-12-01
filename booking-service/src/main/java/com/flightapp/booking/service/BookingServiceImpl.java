package com.flightapp.booking.service;

import com.flightapp.booking.client.FlightServiceWebClient;
import com.flightapp.booking.constants.BookingStatus;
import com.flightapp.booking.dto.BookRequest;
import com.flightapp.booking.dto.BookingResponse;
import com.flightapp.booking.dto.FlightDto;
import com.flightapp.booking.entity.Booking;
import com.flightapp.booking.event.BookingEvent;
import com.flightapp.booking.exception.BookingNotFoundException;
import com.flightapp.booking.repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightServiceWebClient flightServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    @Override
    @CircuitBreaker(name = "flightService", fallbackMethod = "bookTicketFallback")
    @Retry(name = "flightService")
    public Mono<BookingResponse> bookTicket(String flightId, BookRequest request) {
        return flightServiceClient.getFlightById(flightId)
                .flatMap(flight -> {
                    if (flight.getAvailableSeats() < request.getNoOfSeats()) {
                        return Mono.error(new IllegalArgumentException("Not enough seats available"));
                    }

                    return flightServiceClient.updateSeats(flightId, request.getNoOfSeats())
                            .flatMap(updatedFlight -> createBooking(flightId, flight, request));
                })
                .flatMap(booking -> {
                    publishBookingEvent(booking, "BOOKING_CONFIRMED");
                    return Mono.just(mapToResponse(booking));
                })
                .doOnSuccess(response -> log.info("Booking created with PNR: {}", response.getPnr()))
                .doOnError(error -> log.error("Error creating booking: {}", error.getMessage()));
    }

    private Mono<Booking> createBooking(String flightId, FlightDto flight, BookRequest request) {
        String pnr = generatePNR();

        Booking booking = Booking.builder()
                .pnr(pnr)
                .flightId(flightId)
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .fromPlace(flight.getFromPlace())
                .toPlace(flight.getToPlace())
                .departureDateTime(flight.getDepartureDateTime())
                .arrivalDateTime(flight.getArrivalDateTime())
                .userName(request.getUserName())
                .userEmail(request.getUserEmail())
                .journeyDate(request.getJourneyDate())
                .noOfSeats(request.getNoOfSeats())
                .mealType(request.getMealType())
                .totalAmount(flight.getTicketPrice() * request.getNoOfSeats())
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingDateTime(LocalDateTime.now())
                .passengers(request.getPassengers())
                .build();

        return bookingRepository.save(booking);
    }

    @Override
    public Mono<BookingResponse> getBookingByPnr(String pnr) {
        return bookingRepository.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new BookingNotFoundException("Booking not found with PNR: " + pnr)))
                .map(this::mapToResponse);
    }

    @Override
    public Flux<BookingResponse> getBookingHistory(String email) {
        return bookingRepository.findByUserEmail(email)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<BookingResponse> cancelBooking(String pnr) {
        return bookingRepository.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new BookingNotFoundException("Booking not found with PNR: " + pnr)))
                .flatMap(booking -> {
                    if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
                        return Mono.error(new IllegalArgumentException("Booking is already cancelled"));
                    }

                    booking.setBookingStatus(BookingStatus.CANCELLED);
                    return bookingRepository.save(booking);
                })
                .flatMap(booking -> {
                    publishBookingEvent(booking, "BOOKING_CANCELLED");
                    return Mono.just(mapToResponse(booking));
                })
                .doOnSuccess(response -> log.info("Booking cancelled with PNR: {}", pnr));
    }

    private void publishBookingEvent(Booking booking, String eventType) {
        BookingEvent event = BookingEvent.builder()
                .pnr(booking.getPnr())
                .userEmail(booking.getUserEmail())
                .userName(booking.getUserName())
                .flightNumber(booking.getFlightNumber())
                .fromPlace(booking.getFromPlace())
                .toPlace(booking.getToPlace())
                .departureDateTime(booking.getDepartureDateTime())
                .totalAmount(booking.getTotalAmount())
                .eventType(eventType)
                .build();

        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.info("Published booking event: {} for PNR: {}", eventType, booking.getPnr());
    }

    private String generatePNR() {
        return "PNR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .pnr(booking.getPnr())
                .flightId(booking.getFlightId())
                .flightNumber(booking.getFlightNumber())
                .airline(booking.getAirline())
                .fromPlace(booking.getFromPlace())
                .toPlace(booking.getToPlace())
                .departureDateTime(booking.getDepartureDateTime())
                .arrivalDateTime(booking.getArrivalDateTime())
                .userName(booking.getUserName())
                .userEmail(booking.getUserEmail())
                .journeyDate(booking.getJourneyDate())
                .noOfSeats(booking.getNoOfSeats())
                .mealType(booking.getMealType())
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus())
                .bookingDateTime(booking.getBookingDateTime())
                .passengers(booking.getPassengers())
                .build();
    }

    private Mono<BookingResponse> bookTicketFallback(String flightId, BookRequest request, Exception ex) {
        log.error("Circuit breaker fallback - Flight service is unavailable: {}", ex.getMessage());
        return Mono.error(new RuntimeException("Flight service is currently unavailable. Please try again later."));
    }
}
