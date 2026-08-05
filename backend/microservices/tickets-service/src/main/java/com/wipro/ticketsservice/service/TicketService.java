package com.wipro.ticketsservice.service;

import java.util.List;

import com.wipro.ticketsservice.entity.Ticket;

public interface TicketService {
    Ticket createTicket(Ticket ticket);
    List<Ticket> createMultipleTickets(Ticket template, int quantity);
    Ticket getTicketById(int id);
    List<Ticket> getAllTickets();
    Ticket updateTicket(int id, Ticket ticket);
    void deleteTicket(int id);
    List<Ticket> getTicketsByEventId(int eventId);
    List<Ticket> getTicketsByUserId(int userId);
}
