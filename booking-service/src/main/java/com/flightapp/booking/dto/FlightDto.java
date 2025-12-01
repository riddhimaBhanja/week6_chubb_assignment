package com.flightapp.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightDto {
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
    private Double oneWayPrice;
    private Double roundTripPrice;
    private Boolean mealAvailable;
}
