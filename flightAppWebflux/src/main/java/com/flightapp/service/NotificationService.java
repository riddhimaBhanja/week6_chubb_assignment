package com.flightapp.service;

import com.flightapp.dto.NotificationRequest;
import reactor.core.publisher.Mono;

public interface NotificationService {

    Mono<String> sendNotification(NotificationRequest request);
}
