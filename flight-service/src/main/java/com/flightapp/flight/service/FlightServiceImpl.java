package com.flightapp.flight.service;

import com.flightapp.flight.constants.FlightStatus;
import com.flightapp.flight.dto.FlightSearchRequest;
import com.flightapp.flight.dto.InventoryRequest;
import com.flightapp.flight.entity.FlightInventory;
import com.flightapp.flight.exception.FlightNotFoundException;
import com.flightapp.flight.repository.FlightInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightServiceImpl implements FlightService {

    private final FlightInventoryRepository flightInventoryRepository;

    @Override
    public Mono<FlightInventory> addInventory(InventoryRequest request) {
        FlightInventory flight = FlightInventory.builder()
                .airline(request.getAirline())
                .flightNumber(request.getFlightNumber())
                .fromPlace(request.getFromPlace())
                .toPlace(request.getToPlace())
                .departureDateTime(request.getDepartureDateTime())
                .arrivalDateTime(request.getArrivalDateTime())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .ticketPrice(request.getTicketPrice())
                .oneWayPrice(request.getOneWayPrice())
                .roundTripPrice(request.getRoundTripPrice())
                .mealAvailable(request.getMealAvailable())
                .flightStatus(FlightStatus.ACTIVE)
                .build();

        return flightInventoryRepository.save(flight)
                .doOnSuccess(saved -> log.info("Flight inventory added: {}", saved.getFlightNumber()));
    }

    @Override
    public Flux<FlightInventory> searchFlights(FlightSearchRequest request) {
        LocalDate journeyDate = request.getJourneyDate();
        LocalDateTime startOfDay = journeyDate.atStartOfDay();
        LocalDateTime endOfDay = journeyDate.atTime(LocalTime.MAX);

        return flightInventoryRepository.findByFromPlaceAndToPlaceAndDepartureDateTimeBetween(
                        request.getFromPlace(),
                        request.getToPlace(),
                        startOfDay,
                        endOfDay
                )
                .filter(flight -> flight.getFlightStatus() == FlightStatus.ACTIVE)
                .filter(flight -> flight.getAvailableSeats() > 0)
                .doOnComplete(() -> log.info("Flight search completed for {} to {}",
                        request.getFromPlace(), request.getToPlace()));
    }

    @Override
    public Mono<FlightInventory> getFlightById(String flightId) {
        return flightInventoryRepository.findById(flightId)
                .switchIfEmpty(Mono.error(new FlightNotFoundException("Flight not found with id: " + flightId)));
    }

    @Override
    public Mono<FlightInventory> updateAvailableSeats(String flightId, Integer seatsToReduce) {
        return flightInventoryRepository.findById(flightId)
                .switchIfEmpty(Mono.error(new FlightNotFoundException("Flight not found with id: " + flightId)))
                .flatMap(flight -> {
                    if (flight.getAvailableSeats() < seatsToReduce) {
                        return Mono.error(new IllegalArgumentException("Not enough available seats"));
                    }
                    flight.setAvailableSeats(flight.getAvailableSeats() - seatsToReduce);
                    return flightInventoryRepository.save(flight);
                })
                .doOnSuccess(updated -> log.info("Updated available seats for flight: {}", flightId));
    }
}
