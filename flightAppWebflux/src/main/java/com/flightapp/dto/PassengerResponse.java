package com.flightapp.dto;

import lombok.Data;

@Data
public class PassengerResponse {
    private String name;
    private String gender;
    private Integer age;
    private String seatNumber;
}
