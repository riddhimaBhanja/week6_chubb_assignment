package com.flightapp.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "notification_log")
public class NotificationLog {
    @Id
    private String id;

    private String passengerId;
    private String email;
    private String subject;
    private String type;     // NotificationType.name()
    private String status;   // SUCCESS / FAILURE
    private String details;  // error message or info
    private Instant createdAt;
}
