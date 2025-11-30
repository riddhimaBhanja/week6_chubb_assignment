package com.flightapp.repository;

import com.flightapp.entity.NotificationLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface NotificationLogRepository extends ReactiveMongoRepository<NotificationLog, String> {
    Flux<NotificationLog> findByPassengerId(String passengerId);
}
