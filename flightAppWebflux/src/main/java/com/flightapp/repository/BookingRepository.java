package com.flightapp.repository;

import com.flightapp.entity.Booking;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingRepository 
        extends ReactiveMongoRepository<Booking, String> {

    Flux<Booking> findByUserEmail(String userEmail);

    Mono<Booking> findByPnr(String pnr);

    Flux<Booking> findByUserEmailOrderByBookingDateTimeDesc(String userEmail);
}
