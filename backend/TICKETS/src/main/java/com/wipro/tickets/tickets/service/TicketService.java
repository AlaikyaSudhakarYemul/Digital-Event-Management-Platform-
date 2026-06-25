package com.wipro.tickets.tickets.service;

import java.util.List;

import com.wipro.tickets.tickets.dto.TicketDTO;
import com.wipro.tickets.tickets.entity.Ticket;
import com.wipro.tickets.tickets.entity.TicketStatus;

public interface TicketService {

    TicketDTO bookTicket(Ticket ticket, String authorizationHeader);

    TicketDTO getTicketById(Long ticketId);

    List<TicketDTO> getAllTickets();

    List<TicketDTO> getTicketsByUserId(int userId);

    List<TicketDTO> getTicketsByEventId(int eventId);

    TicketDTO cancelTicket(Long ticketId);

    TicketDTO updateTicketStatus(Long ticketId, TicketStatus status);

    List<TicketDTO> getActiveTickets();

    void deleteTicket(Long ticketId);
}
