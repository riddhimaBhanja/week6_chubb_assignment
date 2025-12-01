package com.flightapp.flight.repository;

import com.flightapp.flight.entity.FlightInventory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface FlightInventoryRepository extends ReactiveMongoRepository<FlightInventory, String> {

    Flux<FlightInventory> findByFromPlaceAndToPlaceAndDepartureDateTimeBetween(
            String fromPlace,
            String toPlace,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}
