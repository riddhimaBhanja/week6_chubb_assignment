package com.flightapp.dto;

import com.flightapp.notification.NotificationType;
import lombok.Data;

import java.util.Map;

@Data
public class AdvancedNotificationRequest {
    private String passengerId;
    private NotificationType type;
    private String subject;
    private Map<String, Object> templateVars;
    private boolean attachPdf;
    private boolean sendSms;
    private String smsNumber; // optional, if sendSms true
}
