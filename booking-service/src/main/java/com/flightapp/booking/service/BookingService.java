package com.flightapp.booking.service;

import com.flightapp.booking.dto.BookRequest;
import com.flightapp.booking.dto.BookingResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {

    Mono<BookingResponse> bookTicket(String flightId, BookRequest request);

    Mono<BookingResponse> getBookingByPnr(String pnr);

    Flux<BookingResponse> getBookingHistory(String email);

    Mono<BookingResponse> cancelBooking(String pnr);
}
