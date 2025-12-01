package com.flightapp.booking.dto;

import com.flightapp.booking.constants.BookingStatus;
import com.flightapp.booking.constants.MealType;
import com.flightapp.booking.entity.Passenger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String pnr;
    private String flightId;
    private String flightNumber;
    private String airline;
    private String fromPlace;
    private String toPlace;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;

    private String userName;
    private String userEmail;
    private LocalDate journeyDate;
    private Integer noOfSeats;
    private MealType mealType;
    private Double totalAmount;

    private BookingStatus bookingStatus;
    private LocalDateTime bookingDateTime;
    private List<Passenger> passengers;
}
