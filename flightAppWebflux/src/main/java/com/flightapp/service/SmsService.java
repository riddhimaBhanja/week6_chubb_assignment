package com.flightapp.service;

import reactor.core.publisher.Mono;

public interface SmsService {
    Mono<Boolean> sendSms(String toPhoneNumber, String message);
}
