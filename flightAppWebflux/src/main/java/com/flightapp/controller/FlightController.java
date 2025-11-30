package com.flightapp.controller;

import com.flightapp.dto.*;
import com.flightapp.entity.FlightInventory;
import com.flightapp.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
/* @RequestMapping("/api/${api.version}/flight") */
@RequestMapping("/api/v1/flight")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping("/airline/inventory")
    public Mono<ResponseEntity<FlightInventory>> addInventory(@RequestBody InventoryRequest request) {
        return flightService.addInventory(request).map(saved -> ResponseEntity.status(201).body(saved));
    }


    @PostMapping("/search")
    public Flux<FlightInventory> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        return flightService.searchFlights(request);
    }

    @PostMapping("/booking/{flightId}")
    public Mono<BookingResponse> bookTicket(@PathVariable String flightId, @Valid @RequestBody BookRequest request) {
        return flightService.bookTicket(flightId, request);
    }

    @GetMapping("/ticket/{pnr}")
    public Mono<BookingResponse> getTicket(@PathVariable String pnr) {
        return flightService.getTicketByPnr(pnr);
    }

    @GetMapping("/booking/history/{emailId}")
    public Flux<BookingResponse> history(@PathVariable String emailId) {
        return flightService.getBookingHistory(emailId);
    }

	
	 @DeleteMapping("/cancel/{pnr}") public Mono<BookingResponse>
	  cancelTicket(@PathVariable String pnr) { return
	  flightService.cancelTicket(pnr); }
	 
  

}
