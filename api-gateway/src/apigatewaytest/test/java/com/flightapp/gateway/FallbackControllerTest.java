package com.flightapp.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(FallbackController.class)
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testFlightServiceFallback() {
        webTestClient.get()
                .uri("/fallback/flight")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Flight Service is currently unavailable. Please try again later.")
                .jsonPath("$.status").isEqualTo("SERVICE_UNAVAILABLE");
    }

    @Test
    void testBookingServiceFallback() {
        webTestClient.get()
                .uri("/fallback/booking")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Booking Service is currently unavailable. Please try again later.")
                .jsonPath("$.status").isEqualTo("SERVICE_UNAVAILABLE");
    }
}
