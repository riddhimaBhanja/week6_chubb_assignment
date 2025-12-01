package com.flightapp.booking.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {
    private String name;
    private String gender;
    private Integer age;
    private String seatNumber;
    private String email;
}
