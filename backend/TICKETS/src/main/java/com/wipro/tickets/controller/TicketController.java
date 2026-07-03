package com.wipro.tickets.controller;

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

import com.wipro.tickets.constants.TicketServiceConstants;
import com.wipro.tickets.entity.Ticket;
import com.wipro.tickets.entity.TicketType;
import com.wipro.tickets.service.TicketService;

@RestController
@RequestMapping(TicketServiceConstants.API_URL + TicketServiceConstants.TICKETS_URL)
@CrossOrigin(origins = TicketServiceConstants.FRONTEND_URL)
public class TicketController {

	private final TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@PostMapping("/create")
	public ResponseEntity<?> createTicket(@RequestBody Ticket ticket) {
		try {
			Ticket createdTicket = ticketService.createTicket(ticket);
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
			return ResponseEntity.badRequest().body("Invalid ticket request payload.");
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateTicket(@RequestBody Ticket ticket, @PathVariable int id) {
		try {
			return new ResponseEntity<>(ticketService.updateTicket(id, ticket), HttpStatus.OK);
		} catch (RuntimeException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getTicketById(@PathVariable int id) {
		try {
			return new ResponseEntity<>(ticketService.getTicketById(id), HttpStatus.OK);
		} catch (RuntimeException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
		}
	}

	@GetMapping("/all")
	public ResponseEntity<List<Ticket>> getAllTickets() {
		return new ResponseEntity<>(ticketService.getAllTickets(), HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteTicket(@PathVariable int id) {
		try {
			ticketService.deleteTicket(id);
			return ResponseEntity.noContent().build();
		} catch (RuntimeException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
		}
	}

	@GetMapping("/event/{eventId}")
	public ResponseEntity<List<Ticket>> getTicketsByEventId(@PathVariable int eventId) {
		return new ResponseEntity<>(ticketService.getTicketsByEventId(eventId), HttpStatus.OK);
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Ticket>> getTicketsByUserId(@PathVariable int userId) {
		return new ResponseEntity<>(ticketService.getTicketsByUserId(userId), HttpStatus.OK);
	}
}