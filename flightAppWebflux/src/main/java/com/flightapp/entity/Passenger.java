package com.flightapp.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "passenger")
public class Passenger {

    @Id
    private String id;

    private String name;
    private String gender;
    private Integer age;
    private String seatNumber;

    private String email;       // Added email field

    private Booking booking;    // nested booking object (non-JPA)
}
