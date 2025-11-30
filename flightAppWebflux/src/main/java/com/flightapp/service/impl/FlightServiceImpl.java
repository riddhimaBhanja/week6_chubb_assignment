package com.flightapp.service.impl;

import com.flightapp.dto.*;
import com.flightapp.entity.Booking;
import com.flightapp.entity.FlightInventory;
import com.flightapp.entity.Passenger;
import com.flightapp.exception.FlightNotFoundException;
import com.flightapp.exception.PNRNotFoundException;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightInventoryRepository;
import com.flightapp.service.FlightService;

import constants.BookingStatus;
import constants.FlightStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightInventoryRepository flightInventoryRepository;
    private final BookingRepository bookingRepository;

    @Override
    public Mono<FlightInventory> addInventory(InventoryRequest request) {
    	
    	FlightInventory flight = FlightInventory.builder()
    	        .id(UUID.randomUUID().toString())
    	        .flightNumber(request.getFlightNumber())
    	        .airline(request.getAirline())
    	        .fromPlace(request.getFromPlace())
    	        .toPlace(request.getToPlace())
    	        .departureDateTime(request.getDepartureDateTime())
    	        .totalSeats(request.getTotalSeats())
    	        .ticketPrice(request.getTicketPrice())
    	        .flightStatus(FlightStatus.ACTIVE)
    	        .build();

    	return flightInventoryRepository.save(flight);

    }

    @Override
    public Flux<FlightInventory> searchFlights(FlightSearchRequest request) {
        return flightInventoryRepository.findFlightsForDay(
                request.getFromPlace(),
                request.getToPlace(),
                request.getJourneyDate().atStartOfDay(),
                request.getJourneyDate().atTime(LocalTime.MAX)
        );
    }

    @Override
    public Mono<BookingResponse> bookTicket(String flightId, BookRequest request) {
        return flightInventoryRepository.findById(flightId)
                .switchIfEmpty(Mono.error(new FlightNotFoundException("Flight not found")))
                .flatMap(flight -> {

                    String pnr = "PNR" + System.currentTimeMillis();

                    Booking booking = Booking.builder()
                            .id(pnr)
                            .pnr(pnr)
                            .flightId(flightId)
                            .userName(request.getPassengerName())
                            .userEmail(request.getUserEmail())
                            .journeyDate(request.getJourneyDate())
                            .noOfSeats(request.getNoOfSeats())
                            .mealType(request.getMealType())
                            .bookingStatus(BookingStatus.CONFIRMED)
                            .bookingDateTime(LocalDateTime.now())
                            .build();

                    return bookingRepository.save(booking);
                })
                .map(this::toBookingResponse);
    }


    @Override
    public Mono<BookingResponse> getTicketByPnr(String pnr) {
        return bookingRepository.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new PNRNotFoundException("PNR not found")))
                .map(this::toBookingResponse);
    }

    @Override
    public Flux<BookingResponse> getBookingHistory(String email) {
        return bookingRepository.findByUserEmail(email)
                .map(this::toBookingResponse);
    }

    @Override
    public Mono<BookingResponse> cancelTicket(String pnr) {
        return bookingRepository.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new PNRNotFoundException("PNR not found")))
                .flatMap(booking -> {
                    booking.setBookingStatus(BookingStatus.CANCELLED);
                    return bookingRepository.save(booking).map(this::toBookingResponse);
                });
    }

    private BookingResponse toBookingResponse(Booking saved) {
        return BookingResponse.builder()
                .pnr(saved.getPnr())
                .flightId(saved.getFlightId())
                .userEmail(saved.getUserEmail())
                .userName(saved.getUserName())
                .journeyDate(saved.getJourneyDate())
                .noOfSeats(saved.getNoOfSeats())
                .mealType(saved.getMealType())
                .bookingStatus(saved.getBookingStatus())
                .bookingDateTime(saved.getBookingDateTime())
                .build();
    }

}
