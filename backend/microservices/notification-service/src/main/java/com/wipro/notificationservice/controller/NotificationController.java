package com.wipro.notificationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.notificationservice.dto.NotificationRequest;
import com.wipro.notificationservice.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/registration-confirm")
    public ResponseEntity<?> sendRegistrationConfirmation(@RequestBody NotificationRequest request) {
        notificationService.sendRegistrationConfirmation(request);
        return ResponseEntity.ok("Registration confirmation sent.");
    }

    @PostMapping("/calendar-invite")
    public ResponseEntity<?> sendCalendarInvite(@RequestBody NotificationRequest request) {
        notificationService.sendCalendarInvite(request);
        return ResponseEntity.ok("Calendar invite sent.");
    }

    @PostMapping("/ticket-confirm")
    public ResponseEntity<?> sendTicketConfirmation(@RequestBody NotificationRequest request) {
        notificationService.sendTicketConfirmation(request);
        return ResponseEntity.ok("Ticket confirmation sent.");
    }
}
