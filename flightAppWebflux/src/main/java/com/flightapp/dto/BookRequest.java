package com.flightapp.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

import constants.MealType;

@Data
@Builder
public class BookRequest {
    private String passengerName;
    private String userEmail;
    private LocalDate journeyDate;
    private Integer noOfSeats;
    private MealType mealType;
    private List<PassengerRequest> passengers;
}
