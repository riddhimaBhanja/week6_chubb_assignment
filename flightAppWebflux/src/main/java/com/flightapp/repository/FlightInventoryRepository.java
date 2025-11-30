

package com.flightapp.repository;

import com.flightapp.entity.FlightInventory;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;

public interface FlightInventoryRepository 
        extends ReactiveMongoRepository<FlightInventory, String> {

	@Query("{'fromPlace': ?0, 'toPlace': ?1, 'departureDateTime': { $gte: ?2, $lte: ?3 } }")
	Flux<FlightInventory> findFlightsForDay(
	        String fromPlace,
	        String toPlace,
	        LocalDateTime startOfDay,
	        LocalDateTime endOfDay
	);

    Flux<FlightInventory> findByAirline(String airline);

    
}

