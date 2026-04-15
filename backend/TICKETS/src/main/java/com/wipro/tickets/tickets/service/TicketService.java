package com.wipro.tickets.tickets.service;

import java.util.List;

import com.wipro.tickets.tickets.dto.TicketDTO;
import com.wipro.tickets.tickets.entity.Ticket;

public interface TicketService {

    TicketDTO bookTicket(Ticket ticket);

    TicketDTO getTicketById(Long ticketId);

    List<TicketDTO> getAllTickets();

    List<TicketDTO> getTicketsByUserId(int userId);

    List<TicketDTO> getTicketsByEventId(int eventId);

    TicketDTO cancelTicket(Long ticketId);

    void deleteTicket(Long ticketId);
}
