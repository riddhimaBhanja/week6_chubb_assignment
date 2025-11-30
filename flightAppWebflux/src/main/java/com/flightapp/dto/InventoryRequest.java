package com.flightapp.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryRequest {

    @NotBlank
    private String airlineCode;

    @NotBlank
    private String flightNumber;

    @NotBlank
    private String fromPlace;

    @NotBlank
    private String toPlace;
    private String airline;
    private Double ticketPrice;

    @NotNull
    private LocalDateTime departureDateTime;

    @NotNull
    private LocalDateTime arrivalDateTime;

    @NotNull @Min(1)
    private Integer totalSeats;

    @NotNull @Positive
    private Double oneWayPrice;

    private Double roundTripPrice;

    private Boolean mealAvailable;
}