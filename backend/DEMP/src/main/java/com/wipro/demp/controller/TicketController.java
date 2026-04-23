package com.wipro.demp.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.demp.constants.DempConstants;
import com.wipro.demp.entity.Ticket;
import com.wipro.demp.entity.TicketType;
import com.wipro.demp.service.TicketService;

@RestController
@RequestMapping(DempConstants.API_URL + DempConstants.TICKETS_URL)
@CrossOrigin(origins = DempConstants.FRONTEND_URL)
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket) {
        try {
            Ticket createdTicket = ticketService.createTicket(ticket);
            if (createdTicket == null) {
                return ResponseEntity.badRequest().body("Invalid ticket data.");
            }
            return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/create-multiple")
    public ResponseEntity<?> createMultipleTickets(@RequestBody Map<String, Object> payload) {
        try {
            if (payload == null) {
                return ResponseEntity.badRequest().body("Request body is required.");
            }

            if (payload.get("ticketType") == null) {
                return ResponseEntity.badRequest().body("ticketType is required.");
            }
            if (payload.get("price") == null) {
                return ResponseEntity.badRequest().body("price is required.");
            }
            if (payload.get("eventId") == null) {
                return ResponseEntity.badRequest().body("eventId is required.");
            }
            if (payload.get("userId") == null) {
                return ResponseEntity.badRequest().body("userId is required.");
            }
            if (payload.get("registrationId") == null) {
                return ResponseEntity.badRequest().body("registrationId is required.");
            }

            int quantity = payload.get("quantity") == null
                    ? 1
                    : Integer.parseInt(payload.get("quantity").toString());

            Ticket ticket = new Ticket();
            ticket.setTicketType(TicketType.valueOf(payload.get("ticketType").toString()));
            ticket.setPrice(new BigDecimal(payload.get("price").toString()));
            ticket.setEventId(Integer.parseInt(payload.get("eventId").toString()));
            ticket.setUserId(Integer.parseInt(payload.get("userId").toString()));
            ticket.setRegistrationId(Integer.parseInt(payload.get("registrationId").toString()));

            List<Ticket> createdTickets = ticketService.createMultipleTickets(ticket, quantity);
            return new ResponseEntity<>(createdTickets, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Invalid ticket request payload: " + ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@RequestBody Ticket ticket, @PathVariable int id) {
        Ticket updatedTicket = ticketService.updateTicket(id, ticket);

        if (id < 0 || updatedTicket == null) {
            return ResponseEntity.badRequest().body("Invalid ticket data or ticket not found.");
        }
        return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable int id) {
        if (id < 0) {
            return ResponseEntity.badRequest().body("ID must be apositive integer");
        }
        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket not found with id: " + id);
        }
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTickets() {
        return new ResponseEntity<>(ticketService.getAllTickets(), HttpStatus.OK);
    }   

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable int id) {
        if (id < 0) {
            return ResponseEntity.badRequest().body("ID must be a positive integer");
        }
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getTicketsByEventId(@PathVariable int eventId) {
        if (eventId < 0) {
            return ResponseEntity.badRequest().body("Event ID must be a positive integer");
        }
        return new ResponseEntity<>(ticketService.getTicketsByEventId(eventId), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTicketsByUserId(@PathVariable int userId) {
        if (userId < 0) {
            return ResponseEntity.badRequest().body("User ID must be a positive integer");
        }
        return new ResponseEntity<>(ticketService.getTicketsByUserId(userId), HttpStatus.OK);
    }

}
