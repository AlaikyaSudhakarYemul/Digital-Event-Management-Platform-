package com.wipro.tickets.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.wipro.tickets.entity.Ticket;
import com.wipro.tickets.repository.TicketRepository;

@Service
public class TicketServiceImpl implements TicketService {

	private static final int MAX_TICKETS_PER_USER_PER_EVENT = 5;

	private final TicketRepository ticketRepository;

	public TicketServiceImpl(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	@Override
	public Ticket createTicket(Ticket ticket) {
		return createMultipleTickets(ticket, 1).get(0);
	}

	@Override
	public List<Ticket> createMultipleTickets(Ticket ticketTemplate, int quantity) {
		if (ticketTemplate == null) {
			throw new IllegalArgumentException("Ticket data is required.");
		}
		if (quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be at least 1.");
		}
		if (quantity > MAX_TICKETS_PER_USER_PER_EVENT) {
			throw new IllegalArgumentException("You can select at most 5 tickets at a time.");
		}
		if (ticketTemplate.getUserId() <= 0 || ticketTemplate.getEventId() <= 0) {
			throw new IllegalArgumentException("Valid userId and eventId are required.");
		}

		long existingCount = ticketRepository.countByUserIdAndEventIdAndIsDeletedFalse(
			ticketTemplate.getUserId(), ticketTemplate.getEventId());

		int remainingAllowed = (int) (MAX_TICKETS_PER_USER_PER_EVENT - existingCount);
		if (remainingAllowed <= 0) {
			throw new IllegalArgumentException("Maximum limit reached. You can buy only 5 tickets for this event.");
		}
		if (quantity > remainingAllowed) {
			throw new IllegalArgumentException("You can buy only " + remainingAllowed
				+ " more ticket(s) for this event. Maximum limit is 5.");
		}

		List<Ticket> toSave = new ArrayList<>();
		for (int index = 0; index < quantity; index++) {
			Ticket ticket = new Ticket();
			ticket.setTicketType(ticketTemplate.getTicketType());
			ticket.setPrice(ticketTemplate.getPrice());
			ticket.setEventId(ticketTemplate.getEventId());
			ticket.setUserId(ticketTemplate.getUserId());
			ticket.setRegistrationId(ticketTemplate.getRegistrationId());
			ticket.setCreatedOn(LocalDate.now());
			ticket.setCreationTime(LocalDateTime.now());
			ticket.setUpdatedOn(LocalDate.now());
			ticket.setDeleted(false);
			toSave.add(ticket);
		}

		return ticketRepository.saveAll(toSave);
	}

	@Override
	public Ticket updateTicket(int id, Ticket ticket) {
		Ticket existingTicket = ticketRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

		existingTicket.setEventId(ticket.getEventId());
		existingTicket.setUserId(ticket.getUserId());
		existingTicket.setRegistrationId(ticket.getRegistrationId());
		existingTicket.setPrice(ticket.getPrice());
		existingTicket.setTicketType(ticket.getTicketType());
		existingTicket.setUpdatedOn(LocalDate.now());

		return ticketRepository.save(existingTicket);
	}

	@Override
	public void deleteTicket(int id) {
		Ticket ticket = ticketRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
		ticket.setDeleted(true);
		ticket.setDeletedOn(LocalDate.now());
		ticket.setUpdatedOn(LocalDate.now());
		ticketRepository.save(ticket);
	}

	@Override
	public Ticket getTicketById(int id) {
		return ticketRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
	}

	@Override
	public List<Ticket> getAllTickets() {
		return ticketRepository.findAll();
	}

	@Override
	public List<Ticket> getTicketsByEventId(int eventId) {
		return ticketRepository.findByEventId(eventId);
	}

	@Override
	public List<Ticket> getTicketsByUserId(int userId) {
		return ticketRepository.findByUserId(userId);
	}
}