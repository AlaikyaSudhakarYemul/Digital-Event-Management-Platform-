package com.wipro.notificationservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class NotificationRequest {
    private String toEmail;
    private String userName;
    private String eventName;
    private String eventDescription;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String eventType;
    private String address;
    private int registrationId;
}
