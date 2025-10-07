package com.wipro.demp.controller;


import com.wipro.demp.entity.Registrations;
import com.wipro.demp.service.RegistrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "http://localhost:3000")
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        logger.info("Initializing RegistrationController with RegistrationService");
        this.registrationService = registrationService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<Registrations> createRegistration(@RequestBody Registrations registration) {
        logger.info("Creating registration: {}", registration);
        if (registration == null || registration.getUser().getUserId() <= 0
                || registration.getEvent().getEventId() <= 0) {
            logger.error("Invalid registration data: {}", registration);
            return ResponseEntity.badRequest().body(null);
        }
        Registrations saved = registrationService.createRegistration(registration);
        logger.info("Registration created successfully: {}", saved);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Registrations> getRegistration(@PathVariable int id) {
        logger.info("Fetching registration with ID: {}", id);
        if (id <= 0) {
            logger.error("Invalid ID: {}", id);
            return ResponseEntity.badRequest().body(null);
        }
        Registrations reg = registrationService.getRegistrationById(id);
        logger.info("Registration with ID fetched successfully: {}", reg);
        if (reg == null) {
            logger.warn("Registration with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reg);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Registrations>> getAllRegistrations() {
        logger.info("Fetching all registrations");
        List<Registrations> registrations = registrationService.getAllRegistrations();

        logger.info("Total registrations found: {}", registrations.size());

        return ResponseEntity.ok(registrations);
    }

    // @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Registrations>> getRegistrationsByUserId(@PathVariable int userId) {
        logger.info("Fetching registrations for user ID: {}", userId);
        if (userId <= 0) {
            logger.error("Invalid user ID: {}", userId);
            return ResponseEntity.badRequest().body(null);
        }
        List<Registrations> registrations = registrationService.getRegistrationsByUserId(userId);
        logger.info("Registrations for user ID {} found: {}", userId, registrations.size());
        if (registrations.isEmpty()) {
            logger.warn("No registrations found for user ID: {}", userId);
            return ResponseEntity.notFound().build();
        }
        logger.info("Returning registrations for user ID: {}", userId);
        return ResponseEntity.ok(registrations);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Registrations>> getRegistrationsByEventId(@PathVariable int eventId) {
        logger.info("Fetching registrations for event ID: {}", eventId);
        if (eventId <= 0) {
            logger.error("Invalid event ID: {}", eventId);
            return ResponseEntity.badRequest().body(null);
        }
        List<Registrations> registrations = registrationService.getRegistrationsByEventId(eventId);
        logger.info("Registrations for event ID {} found: {}", eventId, registrations.size());
        if (registrations.isEmpty()) {
            logger.warn("No registrations found for event ID: {}", eventId);
            return ResponseEntity.notFound().build();
        }
        logger.info("Returning registrations for event ID: {}", eventId);
        return ResponseEntity.ok(registrations);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Registrations> updateRegistration(@PathVariable int id,
            @RequestBody Registrations registration) {
        logger.info("Updating registration with ID: {}", id);
        if (id <= 0 || registration == null || registration.getUser().getUserId() <= 0
                || registration.getEvent().getEventId() <= 0) {
            logger.error("Invalid ID or registration data for update: ID={}, Registration={}", id, registration);
            return ResponseEntity.badRequest().body(null);
        }
        registration.setRegistrationId(id);
        logger.info("Registration data to update: {}", registration);
        Registrations updated = registrationService.updateRegistration(registration);
        logger.info("Registration with ID {} updated successfully: {}", id, updated);
        if (updated == null) {
            logger.warn("Registration with ID {} not found for update", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Returning updated registration: {}", updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRegistration(@PathVariable int id) {
        logger.info("Deleting registration with ID: {}", id);
        if (id <= 0) {
            logger.error("Invalid ID for deletion: {}", id);
            return ResponseEntity.badRequest().body("Invalid registration ID.");
        }
        registrationService.deleteRegistration(id);
        logger.info("Registration with ID {} deleted successfully", id);
        return ResponseEntity.ok("Registration deleted successfully");
    }

}