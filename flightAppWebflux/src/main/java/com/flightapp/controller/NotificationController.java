package com.flightapp.controller;

import com.flightapp.dto.NotificationRequest;
import com.flightapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public Mono<String> send(@RequestBody NotificationRequest request) {
        return notificationService.sendNotification(request);
    }
}
