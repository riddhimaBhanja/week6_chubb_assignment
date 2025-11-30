package com.flightapp.service;

import com.flightapp.dto.*;
import com.flightapp.entity.FlightInventory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FlightService {

    Mono<FlightInventory> addInventory(InventoryRequest request);

    Flux<FlightInventory> searchFlights(FlightSearchRequest request);

    Mono<BookingResponse> bookTicket(String flightId, BookRequest request);

    Mono<BookingResponse> getTicketByPnr(String pnr);

    Flux<BookingResponse> getBookingHistory(String email);

    Mono<BookingResponse> cancelTicket(String pnr);
}