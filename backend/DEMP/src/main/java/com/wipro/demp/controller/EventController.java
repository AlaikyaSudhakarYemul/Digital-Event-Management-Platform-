package com.wipro.demp.controller;

import java.security.InvalidParameterException;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
 
import com.wipro.demp.entity.Event;
// import com.wipro.demp.entity.Registrations;
// import com.wipro.demp.entity.Users;
import com.wipro.demp.service.EventService;
import com.wipro.demp.service.RegistrationService;
 
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {
 
    private static final Logger logger = LoggerFactory.getLogger(EventController.class.getName());
    private final EventService eventService;
    // private final RegistrationService registrationService;
 
    public EventController(EventService eventService, RegistrationService registrationService) {
        logger.info("Initializing EventController with EventService and RegistrationService");
        this.eventService = eventService;
        // this.registrationService = registrationService;
    }
 
    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        logger.info("Creating event: {}", event);
        Event createdEvent = eventService.createEvent(event);
        logger.info("Event created successfully: {}", createdEvent);
        if (createdEvent == null) {
            logger.error("Failed to create event: {}", event);
            return ResponseEntity.badRequest().body("Invalid event data.");
        }
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable int id) {
        logger.info("Fetching event with ID: {}", id);
        if (id < 0) {
            logger.error("Invalid ID: {}", id);
            throw new InvalidParameterException("ID must be a positive integer");
        }
        logger.info("Retrieving event with ID: {}", id);
        Event event = eventService.getEventById(id);
        logger.info("Event with ID fetched successfully: {}",event);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }
 
    @GetMapping("/all")
    public ResponseEntity<?> getAllEvents() {
        logger.info("Fetching all events");
        /*if (eventService.getAllEvents().isEmpty()) {
            logger.warn("No events found");
            return ResponseEntity.noContent().build();
        }*/
        logger.info("All events fetched successfully");
        return new ResponseEntity<>(eventService.getAllEvents(), HttpStatus.OK);
    }
 
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable int id, @RequestBody Event updatedEvent) {
        logger.info("Updating event with ID: {}", id);
        if (id < 0 || updatedEvent == null) {
            logger.error("Invalid ID or request body for update: ID={}, Event={}", id, updatedEvent);
            return ResponseEntity.badRequest().body("Invalid request body.");
        }
        Event event = eventService.updateEvent(id, updatedEvent);
        logger.info("Event with ID updated successfully: {}", event);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable int id) {
        logger.info("Deleting event with ID: {}", id);
        if (id < 0) {
            logger.error("Invalid ID for deletion: {}", id);
            throw new InvalidParameterException("ID must be a positive integer");
        }
        eventService.deleteEvent(id);
        logger.info("Event with ID deleted successfully: {}", id);
        return ResponseEntity.ok("Event deleted successfully");
    }
 
    @GetMapping("/search")
    public ResponseEntity<?> searchEvents(@RequestParam String eventName) {
        logger.info("Searching for events with name: {}", eventName);
        if (eventName == null || eventName.isEmpty()) {
            logger.error("Invalid event name: {}", eventName);
            return ResponseEntity.badRequest().body("Invalid event name.");
        }
        logger.info("Events with name '{}' fetched successfully", eventName);
        return new ResponseEntity<>(eventService.findByEventName(eventName), HttpStatus.OK);
    }
 
    // @PreAuthorize("hasRole('USER')")
    // @PostMapping("/{id}/register")
    // public ResponseEntity<?> registerForEvent(@PathVariable int id) {
    //     logger.info("Registering user for event with ID: {}", id);
    //     if (id < 0) {
    //         logger.error("Invalid event ID: {}", id);
    //         return ResponseEntity.badRequest().body("Invalid event ID.");
    //     }
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     if (authentication == null || !(authentication.getPrincipal() instanceof Users)) {
    //         logger.error("User not authenticated");
    //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    //     }
    //     Users user = (Users) authentication.getPrincipal();
    //     Event event = eventService.getEventById(id);
    //     if (event == null) {
    //         logger.error("Event not found: {}", id);
    //         return ResponseEntity.notFound().build();
    //     }
    //     Registrations registration = new Registrations();
    //     registration.setUser(user);
    //     registration.setEvent(event);
    //     Registrations saved = registrationService.createRegistration(registration);
    //     logger.info("User {} registered for event {}", user.getUserId(), id);
    //     return new ResponseEntity<>(saved, HttpStatus.CREATED);
    // }
 
    @GetMapping("/paginated")
    public ResponseEntity<?> getPaginatedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(required = false) String eventName) {
                logger.info("Fetching paginated events: page={}, size={}, eventName={}", page, size, eventName);
        if (page < 0 || size <= 0) {
            logger.error("Invalid pagination parameters: page={}, size={}", page, size);
            return ResponseEntity.badRequest().body("Invalid pagination parameters.");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = eventService.getPaginatedEvents(eventName, pageable);
        if (eventPage.isEmpty()) {
            logger.warn("No events found for the given criteria");
            return ResponseEntity.noContent().build();
        }
        logger.info("Paginated events fetched successfully: page={}, size={}, totalElements={}",
                    page, size, eventPage.getTotalElements());
        return new ResponseEntity<>(eventPage, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/organizer/{userId}")
    public ResponseEntity<?> getEventsByOrganizerId(@PathVariable Integer userId) {
        // logger.info("Fetching events for organizer with ID: {}", userId);
        if (userId == null || userId < 0) {
            // logger.error("Invalid organizer ID: {}", userId);
            return ResponseEntity.badRequest().body("Invalid organizer ID.");
        }
        // logger.info("Events for organizer with ID {} fetched successfully", userId);
        return new ResponseEntity<>(eventService.findAllEventsByUserId(userId), HttpStatus.OK);
    }
 
}