package com.flightapp.booking.client;

import com.flightapp.booking.dto.FlightDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class FlightServiceWebClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    @Autowired
    public FlightServiceWebClient(WebClient.Builder webClientBuilder, DiscoveryClient discoveryClient) {
        this.webClientBuilder = webClientBuilder;
        this.discoveryClient = discoveryClient;
    }

    private String getFlightServiceUrl() {
        return discoveryClient.getInstances("flight-service")
                .stream()
                .findFirst()
                .map(instance -> instance.getUri().toString())
                .orElse("http://localhost:8081");
    }

    public Mono<FlightDto> getFlightById(String flightId) {
        return webClientBuilder.build()
                .get()
                .uri(getFlightServiceUrl() + "/api/v1/flight/" + flightId)
                .retrieve()
                .bodyToMono(FlightDto.class);
    }

    public Mono<FlightDto> updateSeats(String flightId, Integer seatsToReduce) {
        return webClientBuilder.build()
                .put()
                .uri(getFlightServiceUrl() + "/api/v1/flight/" + flightId + "/seats?seatsToReduce=" + seatsToReduce)
                .retrieve()
                .bodyToMono(FlightDto.class);
    }
}
