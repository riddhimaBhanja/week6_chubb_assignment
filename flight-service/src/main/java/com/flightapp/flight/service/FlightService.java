package com.flightapp.flight.service;

import com.flightapp.flight.dto.FlightSearchRequest;
import com.flightapp.flight.dto.InventoryRequest;
import com.flightapp.flight.entity.FlightInventory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FlightService {

    Mono<FlightInventory> addInventory(InventoryRequest request);

    Flux<FlightInventory> searchFlights(FlightSearchRequest request);

    Mono<FlightInventory> getFlightById(String flightId);

    Mono<FlightInventory> updateAvailableSeats(String flightId, Integer seatsToReduce);
}
