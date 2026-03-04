package com.wipro.demp.service;

import java.util.List;

import com.wipro.demp.entity.Ticket;

public interface TicketService {

    public Ticket createTicket(Ticket ticket);
    public Ticket updateTicket(int id, Ticket ticket);
    public void deleteTicket(int id);
    public Ticket getTicketById(int id);
    public List<Ticket> getAllTickets();
    public List<Ticket> getTicketsByEventId(int eventId);
    
}
