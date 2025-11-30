package com.flightapp.repository;

import com.flightapp.entity.Passenger;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PassengerRepository extends ReactiveMongoRepository<Passenger, String> {

    Flux<Passenger> findByBookingId(Long bookingId);
}
