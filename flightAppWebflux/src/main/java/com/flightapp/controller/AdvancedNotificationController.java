package com.flightapp.controller;

import com.flightapp.dto.AdvancedNotificationRequest;
import com.flightapp.entity.NotificationLog;
import com.flightapp.repository.NotificationLogRepository;
import com.flightapp.service.AdvancedNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class AdvancedNotificationController {

    private final AdvancedNotificationService advancedNotificationService;
    private final NotificationLogRepository logRepository;

    @PostMapping("/advanced/send")
    public Mono<String> sendAdvanced(@RequestBody AdvancedNotificationRequest request) {
        return advancedNotificationService.notify(request);
    }

    @GetMapping("/logs")
    public Flux<NotificationLog> getAllLogs() {
        return logRepository.findAll();
    }

    @GetMapping("/logs/{passengerId}")
    public Flux<NotificationLog> getLogsFor(@PathVariable String passengerId) {
        return logRepository.findByPassengerId(passengerId);
    }
}
