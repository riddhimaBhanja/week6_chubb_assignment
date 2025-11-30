package com.flightapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class FlightSearchRequest {

    @NotBlank
    private String fromPlace;

    @NotBlank
    private String toPlace;

    @NotNull
    private LocalDate journeyDate;
}