package com.flightapp.booking.controller;

import com.flightapp.booking.dto.BookRequest;
import com.flightapp.booking.dto.BookingResponse;
import com.flightapp.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/book/{flightId}")
    public Mono<BookingResponse> bookTicket(@PathVariable String flightId, @Valid @RequestBody BookRequest request) {
        return bookingService.bookTicket(flightId, request);
    }

    @GetMapping("/{pnr}")
    public Mono<BookingResponse> getBooking(@PathVariable String pnr) {
        return bookingService.getBookingByPnr(pnr);
    }

    @GetMapping("/history/{email}")
    public Flux<BookingResponse> getBookingHistory(@PathVariable String email) {
        return bookingService.getBookingHistory(email);
    }

    @DeleteMapping("/cancel/{pnr}")
    public Mono<BookingResponse> cancelBooking(@PathVariable String pnr) {
        return bookingService.cancelBooking(pnr);
    }
}
