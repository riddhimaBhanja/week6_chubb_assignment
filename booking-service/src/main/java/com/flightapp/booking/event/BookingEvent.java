package com.flightapp.booking.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent implements Serializable {
    private String pnr;
    private String userEmail;
    private String userName;
    private String flightNumber;
    private String fromPlace;
    private String toPlace;
    private LocalDateTime departureDateTime;
    private Double totalAmount;
    private String eventType; // BOOKING_CONFIRMED, BOOKING_CANCELLED
}
