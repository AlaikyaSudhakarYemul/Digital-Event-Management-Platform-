package com.wipro.adminservice.controller;

import com.wipro.adminservice.client.EventServiceClient;
import com.wipro.adminservice.client.UserServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;

    public AdminController(UserServiceClient userServiceClient, EventServiceClient eventServiceClient) {
        this.userServiceClient = userServiceClient;
        this.eventServiceClient = eventServiceClient;
    }

    // --- User management ---

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userServiceClient.getAllUsers());
    }

    @GetMapping("/users/organizers")
    public ResponseEntity<?> getAllOrganizers() {
        return ResponseEntity.ok(userServiceClient.getAllOrganizers());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        return ResponseEntity.ok(userServiceClient.deleteUser(id));
    }

    // --- Event management ---

    @GetMapping("/events")
    public ResponseEntity<?> getAllEvents() {
        return ResponseEntity.ok(eventServiceClient.getAllEvents());
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable int id) {
        return ResponseEntity.ok(eventServiceClient.deleteEvent(id));
    }

    // --- Address management ---

    @PostMapping("/address")
    public ResponseEntity<?> addAddress(@RequestBody Map<String, Object> address) {
        return ResponseEntity.ok(eventServiceClient.addAddress(address));
    }

    @GetMapping("/address/{id}")
    public ResponseEntity<?> getAddress(@PathVariable int id) {
        return ResponseEntity.ok(eventServiceClient.getAddress(id));
    }

    // --- Speaker management ---

    @GetMapping("/speakers")
    public ResponseEntity<?> getAllSpeakers() {
        return ResponseEntity.ok(eventServiceClient.getAllSpeakers());
    }

    @PostMapping("/speakers")
    public ResponseEntity<?> createSpeaker(@RequestBody Map<String, Object> speaker) {
        return ResponseEntity.ok(eventServiceClient.createSpeaker(speaker));
    }

    @DeleteMapping("/speakers/{id}")
    public ResponseEntity<?> deleteSpeaker(@PathVariable int id) {
        return ResponseEntity.ok(eventServiceClient.deleteSpeaker(id));
    }
}
