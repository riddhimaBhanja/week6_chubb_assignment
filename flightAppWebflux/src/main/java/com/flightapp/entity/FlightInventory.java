package com.flightapp.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import constants.FlightStatus;

import java.time.LocalDateTime;

@Data
@Document(collection = "flight_inventory")
@Builder
public class FlightInventory {

    @Id
    private String id;

    private String airline;
    private String flightNumber;
    private String fromPlace;
    private String toPlace;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;

    private Integer totalSeats;
    private Integer availableSeats;

    private Double ticketPrice;
    private FlightStatus flightStatus;

    private Double oneWayPrice;
    private Double roundTripPrice;

    private Boolean mealAvailable;
}
