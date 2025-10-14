package com.wipro.event.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.wipro.event.dto.RegistrationsDTO;
import com.wipro.event.entity.Registrations;
import com.wipro.event.service.RegistrationService;

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
    public ResponseEntity<?> createRegistration(@RequestBody Registrations registration) {
        logger.info("Creating registration: {}", registration);
        if (registration == null || registration.getUserId() <= 0
                || registration.getEvent().getEventId() <= 0) {
            logger.error("Invalid registration data: {}", registration);
            return ResponseEntity.badRequest().body(null);
        }
        RegistrationsDTO saved = registrationService.createRegistration(registration);
        logger.info("Registration created successfully: {}", saved);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRegistration(@PathVariable int id) {
        logger.info("Fetching registration with ID: {}", id);
        if (id <= 0) {
            logger.error("Invalid ID: {}", id);
            return ResponseEntity.badRequest().body(null);
        }
        RegistrationsDTO reg = registrationService.getRegistrationById(id);
        logger.info("Registration with ID fetched successfully: {}", reg);
        if (reg == null) {
            logger.warn("Registration with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reg);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllRegistrations() {
        logger.info("Fetching all registrations");
        List<RegistrationsDTO> registrations = registrationService.getAllRegistrations();

        logger.info("Total registrations found: {}", registrations.size());

        return ResponseEntity.ok(registrations);
    }

    // @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getRegistrationsByUserId(@PathVariable int userId) {
        logger.info("Fetching registrations for user ID: {}", userId);
        if (userId <= 0) {
            logger.error("Invalid user ID: {}", userId);
            return ResponseEntity.badRequest().body(null);
        }
        List<RegistrationsDTO> registrations = registrationService.getRegistrationsByUserId(userId);
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
    public ResponseEntity<?> getRegistrationsByEventId(@PathVariable int eventId) {
        logger.info("Fetching registrations for event ID: {}", eventId);
        if (eventId <= 0) {
            logger.error("Invalid event ID: {}", eventId);
            return ResponseEntity.badRequest().body(null);
        }
        List<RegistrationsDTO> registrations = registrationService.getRegistrationsByEventId(eventId);
        logger.info("Registrations for event ID {} found: {}", eventId, registrations.size());
        if (registrations.isEmpty()) {
            logger.warn("No registrations found for event ID: {}", eventId);
            return ResponseEntity.notFound().build();
        }
        logger.info("Returning registrations for event ID: {}", eventId);
        return ResponseEntity.ok(registrations);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRegistration(@PathVariable int id,
            @RequestBody Registrations registration) {
        logger.info("Updating registration with ID: {}", id);
        if (id <= 0 || registration == null || registration.getUserId() <= 0
                || registration.getEvent().getEventId() <= 0) {
            logger.error("Invalid ID or registration data for update: ID={}, Registration={}", id, registration);
            return ResponseEntity.badRequest().body(null);
        }
        registration.setRegistrationId(id);
        logger.info("Registration data to update: {}", registration);
        RegistrationsDTO updated = registrationService.updateRegistration(registration);
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