package com.wipro.eventservice.controller;

import java.security.InvalidParameterException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.wipro.eventservice.entity.Event;
import com.wipro.eventservice.service.EventService;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        Event created = eventService.createEvent(event);
        if (created == null) {
            return ResponseEntity.badRequest().body("Invalid event data.");
        }
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable int id) {
        if (id < 0) throw new InvalidParameterException("ID must be a positive integer");
        return new ResponseEntity<>(eventService.getEventById(id), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllEvents() {
        return new ResponseEntity<>(eventService.getAllEvents(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable int id, @RequestBody Event updatedEvent) {
        if (id < 0 || updatedEvent == null) {
            return ResponseEntity.badRequest().body("Invalid request body.");
        }
        return new ResponseEntity<>(eventService.updateEvent(id, updatedEvent), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable int id) {
        if (id < 0) throw new InvalidParameterException("ID must be a positive integer");
        eventService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchEvents(@RequestParam String eventName) {
        if (eventName == null || eventName.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid event name.");
        }
        return new ResponseEntity<>(eventService.findByEventName(eventName), HttpStatus.OK);
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getPaginatedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(required = false) String eventName) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.badRequest().body("Invalid pagination parameters.");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = eventService.getPaginatedEvents(eventName, pageable);
        if (eventPage.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return new ResponseEntity<>(eventPage, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/organizer/{userId}")
    public ResponseEntity<?> getEventsByOrganizerId(@PathVariable Integer userId) {
        if (userId == null || userId < 0) {
            return ResponseEntity.badRequest().body("Invalid organizer ID.");
        }
        return new ResponseEntity<>(eventService.findAllEventsByUserId(userId), HttpStatus.OK);
    }
}
