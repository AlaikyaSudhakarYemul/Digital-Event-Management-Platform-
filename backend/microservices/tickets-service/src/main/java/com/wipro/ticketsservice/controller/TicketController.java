package com.wipro.ticketsservice.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wipro.ticketsservice.entity.Ticket;
import com.wipro.ticketsservice.entity.TicketType;
import com.wipro.ticketsservice.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "http://localhost:3000")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket) {
        try {
            Ticket created = ticketService.createTicket(ticket);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/create-multiple")
    public ResponseEntity<?> createMultipleTickets(@RequestBody Map<String, Object> payload) {
        try {
            if (payload == null) return ResponseEntity.badRequest().body("Request body is required.");
            int quantity = payload.get("quantity") == null ? 1 : Integer.parseInt(payload.get("quantity").toString());

            Ticket ticket = new Ticket();
            ticket.setTicketType(TicketType.valueOf(payload.get("ticketType").toString()));
            ticket.setPrice(new BigDecimal(payload.get("price").toString()));
            ticket.setEventId(Integer.parseInt(payload.get("eventId").toString()));
            ticket.setUserId(Integer.parseInt(payload.get("userId").toString()));
            ticket.setRegistrationId(Integer.parseInt(payload.get("registrationId").toString()));

            List<Ticket> created = ticketService.createMultipleTickets(ticket, quantity);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Invalid ticket request payload.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable int id, @RequestBody Ticket ticket) {
        if (id < 0) return ResponseEntity.badRequest().body("Invalid ID.");
        return new ResponseEntity<>(ticketService.updateTicket(id, ticket), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable int id) {
        if (id < 0) return ResponseEntity.badRequest().body("ID must be a positive integer");
        return new ResponseEntity<>(ticketService.getTicketById(id), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTickets() {
        return new ResponseEntity<>(ticketService.getAllTickets(), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable int id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok("Ticket deleted successfully");
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getTicketsByEventId(@PathVariable int eventId) {
        return ResponseEntity.ok(ticketService.getTicketsByEventId(eventId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTicketsByUserId(@PathVariable int userId) {
        return ResponseEntity.ok(ticketService.getTicketsByUserId(userId));
    }
}
