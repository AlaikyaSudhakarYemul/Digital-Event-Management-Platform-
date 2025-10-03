package com.wipro.event.controller;

import java.security.InvalidParameterException;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wipro.event.dto.EventDTO;
import com.wipro.event.entity.Event;
import com.wipro.event.service.EventService;
 
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {
 
    private static final Logger logger = LoggerFactory.getLogger(EventController.class.getName());
    private final EventService eventService;
 
    public EventController(EventService eventService) {
        logger.info("Initializing EventController with EventService");
        this.eventService = eventService;
    }
 
    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        logger.info("Creating event: {}", event);
        EventDTO createdEvent = eventService.createEvent(event);
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
        EventDTO event = eventService.getEventById(id);
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
        EventDTO event = eventService.updateEvent(id, updatedEvent);
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
 
    @GetMapping("/paginated")
    public ResponseEntity<?> getPaginatedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String eventName) {
                logger.info("Fetching paginated events: page={}, size={}, eventName={}", page, size, eventName);
        if (page < 0 || size <= 0) {
            logger.error("Invalid pagination parameters: page={}, size={}", page, size);
            return ResponseEntity.badRequest().body("Invalid pagination parameters.");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<EventDTO> eventPage = eventService.getPaginatedEvents(eventName, pageable);
        if (eventPage.isEmpty()) {
            logger.warn("No events found for the given criteria");
            return ResponseEntity.noContent().build();
        }
        logger.info("Paginated events fetched successfully: page={}, size={}, totalElements={}",
                    page, size, eventPage.getTotalElements());
        return new ResponseEntity<>(eventPage, HttpStatus.OK);
    }
 
}