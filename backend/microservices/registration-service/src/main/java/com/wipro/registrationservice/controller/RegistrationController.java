package com.wipro.registrationservice.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.registrationservice.entity.Registrations;
import com.wipro.registrationservice.service.RegistrationService;

@RestController
@RequestMapping("/api/registrations")
<<<<<<< HEAD
=======
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://192.168.1.37:3000"})
>>>>>>> ec1b18ac4aa2a141dcda3e32cc633f2da5b39817
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<?> createRegistration(@RequestBody Map<String, Integer> payload) {
        Integer userId = payload.get("userId");
        Integer eventId = payload.get("eventId");

        if (userId == null || userId <= 0 || eventId == null || eventId <= 0) {
            logger.error("Invalid registration data: userId={}, eventId={}", userId, eventId);
            return ResponseEntity.badRequest().body(null);
        }

        try {
            Registrations saved = registrationService.createRegistration(userId, eventId);
            logger.info("Registration created: {}", saved.getRegistrationId());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.warn("Registration rejected for userId={}, eventId={}: {}", userId, eventId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Registrations> getRegistration(@PathVariable int id) {
        if (id <= 0) return ResponseEntity.badRequest().body(null);
        Registrations reg = registrationService.getRegistrationById(id);
        return ResponseEntity.ok(reg);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Registrations>> getAllRegistrations() {
        return ResponseEntity.ok(registrationService.getAllRegistrations());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Registrations>> getRegistrationsByUserId(@PathVariable int userId) {
        if (userId <= 0) return ResponseEntity.badRequest().body(null);
        return ResponseEntity.ok(registrationService.getRegistrationsByUserId(userId));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Registrations>> getRegistrationsByEventId(@PathVariable int eventId) {
        if (eventId <= 0) return ResponseEntity.badRequest().body(null);
        return ResponseEntity.ok(registrationService.getRegistrationsByEventId(eventId));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelRegistration(@PathVariable int id) {
        registrationService.cancelRegistration(id);
        return ResponseEntity.ok("Registration cancelled successfully");
    }
}
