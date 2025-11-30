package com.flightapp;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = "com.flightapp")
public class FlightAppWebfluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightAppWebfluxApplication.class, args);
	}

}
