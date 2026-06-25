package com.wipro.tickets.tickets.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.tickets.tickets.dto.TicketDTO;
import com.wipro.tickets.tickets.entity.Ticket;
import com.wipro.tickets.tickets.entity.TicketStatus;
import com.wipro.tickets.tickets.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "http://localhost:3000")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookTicket(@Valid @RequestBody Ticket ticket,
                                        @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        logger.info("Booking ticket for userId={}, eventId={}", ticket.getUserId(), ticket.getEventId());
        TicketDTO booked = ticketService.bookTicket(ticket, authorizationHeader);
        return new ResponseEntity<>(booked, HttpStatus.CREATED);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTicket(@Valid @RequestBody Ticket ticket,
                                          @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        logger.info("Creating ticket for userId={}, eventId={}", ticket.getUserId(), ticket.getEventId());
        TicketDTO created = ticketService.bookTicket(ticket, authorizationHeader);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getTicketById(@PathVariable Long ticketId) {
        logger.info("Fetching ticket with ID={}", ticketId);
        return ResponseEntity.ok(ticketService.getTicketById(ticketId));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTickets() {
        logger.info("Fetching all tickets");
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveTickets() {
        logger.info("Fetching active tickets");
        return ResponseEntity.ok(ticketService.getActiveTickets());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTicketsByUserId(@PathVariable int userId) {
        logger.info("Fetching tickets for userId={}", userId);
        List<TicketDTO> tickets = ticketService.getTicketsByUserId(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getTicketsByEventId(@PathVariable int eventId) {
        logger.info("Fetching tickets for eventId={}", eventId);
        List<TicketDTO> tickets = ticketService.getTicketsByEventId(eventId);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{ticketId}/cancel")
    public ResponseEntity<?> cancelTicket(@PathVariable Long ticketId) {
        logger.info("Cancelling ticket with ID={}", ticketId);
        TicketDTO cancelled = ticketService.cancelTicket(ticketId);
        return ResponseEntity.ok(cancelled);
    }

    @PutMapping("/{ticketId}/status/{status}")
    public ResponseEntity<?> updateTicketStatus(@PathVariable Long ticketId, @PathVariable TicketStatus status) {
        logger.info("Updating ticket ID={} to status={}", ticketId, status);
        TicketDTO updated = ticketService.updateTicketStatus(ticketId, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long ticketId) {
        logger.info("Deleting ticket with ID={}", ticketId);
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.ok("Ticket deleted successfully");
    }
}
