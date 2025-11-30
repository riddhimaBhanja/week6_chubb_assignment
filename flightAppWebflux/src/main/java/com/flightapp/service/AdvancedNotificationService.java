package com.flightapp.service;

import com.flightapp.dto.AdvancedNotificationRequest;
import reactor.core.publisher.Mono;

public interface AdvancedNotificationService {
    Mono<String> notify(AdvancedNotificationRequest request);
}
