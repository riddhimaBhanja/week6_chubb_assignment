package com.flightapp.dto;

import lombok.Data;

@Data
public class NotificationRequest {

    private String passengerId;   // the ID whose email we will fetch
    private String subject;       // email subject
    private String message;       // plain text to inject into template
}
