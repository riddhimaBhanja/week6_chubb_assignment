package com.flightapp.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import constants.BookingStatus;
import constants.MealType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "booking")
@Builder
public class Booking {

    @Id
    private String id;

    private String pnr;
    private FlightInventory flight;  // Embedded or stored reference
    private String userName;
    private String userEmail;
    private LocalDate journeyDate;
    private Integer noOfSeats;
    private MealType mealType;
    private String flightId;
    private BookingStatus bookingStatus;   // CONFIRMED / CANCELLED
    private LocalDateTime bookingDateTime;

    private List<Passenger> passengers; // stored as nested list
}
