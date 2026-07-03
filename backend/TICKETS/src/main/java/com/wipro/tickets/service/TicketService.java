package com.wipro.tickets.service;

import java.util.List;

import com.wipro.tickets.entity.Ticket;

public interface TicketService {

	Ticket createTicket(Ticket ticket);

	List<Ticket> createMultipleTickets(Ticket ticketTemplate, int quantity);

	Ticket updateTicket(int id, Ticket ticket);

	void deleteTicket(int id);

	Ticket getTicketById(int id);

	List<Ticket> getAllTickets();

	List<Ticket> getTicketsByEventId(int eventId);

	List<Ticket> getTicketsByUserId(int userId);
}