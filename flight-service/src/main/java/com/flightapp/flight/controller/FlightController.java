package com.flightapp.flight.controller;

import com.flightapp.flight.dto.FlightSearchRequest;
import com.flightapp.flight.dto.InventoryRequest;
import com.flightapp.flight.entity.FlightInventory;
import com.flightapp.flight.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/flight")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping("/inventory")
    public Mono<ResponseEntity<FlightInventory>> addInventory(@RequestBody InventoryRequest request) {
        return flightService.addInventory(request)
                .map(saved -> ResponseEntity.status(201).body(saved));
    }

    @PostMapping("/search")
    public Flux<FlightInventory> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        return flightService.searchFlights(request);
    }

    @GetMapping("/{flightId}")
    public Mono<FlightInventory> getFlightById(@PathVariable String flightId) {
        return flightService.getFlightById(flightId);
    }

    @PutMapping("/{flightId}/seats")
    public Mono<FlightInventory> updateSeats(@PathVariable String flightId, @RequestParam Integer seatsToReduce) {
        return flightService.updateAvailableSeats(flightId, seatsToReduce);
    }
}
