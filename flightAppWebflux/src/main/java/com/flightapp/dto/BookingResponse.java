package com.flightapp.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import constants.BookingStatus;
import constants.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {
    private String pnr;
    private String flightId;
    private String userName;
    private String userEmail;
    private LocalDate journeyDate;
    private Integer noOfSeats;
    private MealType mealType;
    private BookingStatus bookingStatus;
    private LocalDateTime bookingDateTime;
    private List<PassengerResponse> passengers;
}

