package com.flightapp.dto;

import lombok.Data;

@Data
public class PassengerRequest {
    private String name;
    private String gender;
    private Integer age;
    private String seatNumber;
}
