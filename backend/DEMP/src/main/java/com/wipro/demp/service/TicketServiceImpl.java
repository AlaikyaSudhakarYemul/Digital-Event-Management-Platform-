package com.wipro.demp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wipro.demp.entity.Ticket;
import com.wipro.demp.repository.TicketRepository;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Ticket createTicket(Ticket ticket) {
        // TODO Auto-generated method stub

        ticket.setCreatedOn(LocalDate.now());
        ticket.setCreationTime(LocalDateTime.now());
        ticket.setUpdatedOn(LocalDate.now());
        ticket.setDeleted(false);

        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket updateTicket(int id, Ticket ticket) {
        // TODO Auto-generated method stub

        if (ticketRepository.findById(id).isEmpty()) {
            throw new RuntimeException("Ticket not found with id: " + id);
        }

        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isEmpty()) {
            throw new RuntimeException("Ticket not found with id: " + id);
        }
        Ticket existingTicket = ticketOpt.get();
        existingTicket.setEventId(ticket.getEventId());
        existingTicket.setUserId(ticket.getUserId());
        existingTicket.setRegistrationId(ticket.getRegistrationId());
        existingTicket.setPrice(ticket.getPrice());
        existingTicket.setUpdatedOn(LocalDate.now());

        return ticketRepository.save(existingTicket);
    }

    @Override
    public void deleteTicket(int id) {
        // TODO Auto-generated method stub
        if (ticketRepository.findById(id).isEmpty()) {
            throw new RuntimeException("Ticket not found with id: " + id);
        }

        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setDeleted(true);
            ticket.setUpdatedOn(LocalDate.now());
            ticketRepository.save(ticket);
        }
    }

    @Override
    public Ticket getTicketById(int id) {
        // TODO Auto-generated method stub
        if (ticketRepository.findById(id).isEmpty()) {
            throw new RuntimeException("Ticket not found with id: " + id);
        }
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            return ticketOpt.get();
        } else {
            throw new RuntimeException("Ticket not found with id: " + id);
        }
    }

    @Override
    public List<Ticket> getAllTickets() {
        // TODO Auto-generated method stub
        List<Ticket> tickets = ticketRepository.findAll();
        if (tickets.isEmpty()) {
            throw new RuntimeException("No tickets found.");
        }
        return tickets;

    }

    @Override
    public List<Ticket> getTicketsByEventId(int eventId) {
        // TODO Auto-generated method stub
            List<Ticket> tickets = ticketRepository.findByEventId(eventId);
            if (tickets.isEmpty()) {
                throw new RuntimeException("No tickets found for event id: " + eventId);
            }
            return tickets;
    }

}
