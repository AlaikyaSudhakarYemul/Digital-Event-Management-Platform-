package com.wipro.organizerservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.organizerservice.client.EventServiceClient;
import com.wipro.organizerservice.client.RegistrationServiceClient;

@RestController
@RequestMapping("/api/organizer")
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerController {

    private final EventServiceClient eventServiceClient;
    private final RegistrationServiceClient registrationServiceClient;

    public OrganizerController(EventServiceClient eventServiceClient,
                               RegistrationServiceClient registrationServiceClient) {
        this.eventServiceClient = eventServiceClient;
        this.registrationServiceClient = registrationServiceClient;
    }

    @GetMapping("/events/{userId}")
    public ResponseEntity<?> getMyEvents(@PathVariable int userId) {
        return ResponseEntity.ok(eventServiceClient.getEventsByOrganizerId(userId));
    }

    @PostMapping("/events/create")
    public ResponseEntity<?> createEvent(@RequestBody Object event) {
        return ResponseEntity.ok(eventServiceClient.createEvent(event));
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable int id, @RequestBody Object event) {
        return ResponseEntity.ok(eventServiceClient.updateEvent(id, event));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable int id) {
        return ResponseEntity.ok(eventServiceClient.deleteEvent(id));
    }

    @GetMapping("/events/{eventId}/registrations")
    public ResponseEntity<?> getRegistrationsForEvent(@PathVariable int eventId) {
        return ResponseEntity.ok(registrationServiceClient.getRegistrationsByEventId(eventId));
    }
}
