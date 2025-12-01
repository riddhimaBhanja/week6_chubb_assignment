package com.flightapp.booking.dto;

import com.flightapp.booking.constants.MealType;
import com.flightapp.booking.entity.Passenger;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    @NotBlank(message = "User name is required")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String userEmail;

    @NotNull(message = "Journey date is required")
    private LocalDate journeyDate;

    @NotNull(message = "Number of seats is required")
    private Integer noOfSeats;

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    @NotEmpty(message = "At least one passenger is required")
    private List<Passenger> passengers;
}
