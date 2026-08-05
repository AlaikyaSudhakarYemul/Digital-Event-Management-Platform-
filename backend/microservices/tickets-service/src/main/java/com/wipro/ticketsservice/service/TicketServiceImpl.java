package com.wipro.ticketsservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.wipro.ticketsservice.entity.Ticket;
import com.wipro.ticketsservice.repository.TicketRepository;

@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);
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
    public List<Ticket> createMultipleTickets(Ticket template, int quantity) {
        if (template == null) throw new IllegalArgumentException("Ticket data is required.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be at least 1.");
        if (quantity > MAX_TICKETS_PER_USER_PER_EVENT) {
            throw new IllegalArgumentException("You can select at most 5 tickets at a time.");
        }
        if (template.getUserId() <= 0 || template.getEventId() <= 0) {
            throw new IllegalArgumentException("Valid userId and eventId are required.");
        }

        long existingCount = ticketRepository.countByUserIdAndEventIdAndIsDeletedFalse(
                template.getUserId(), template.getEventId());
        int remainingAllowed = (int) (MAX_TICKETS_PER_USER_PER_EVENT - existingCount);

        if (remainingAllowed <= 0) {
            throw new IllegalArgumentException("Maximum limit reached. You can buy only 5 tickets for this event.");
        }
        if (quantity > remainingAllowed) {
            throw new IllegalArgumentException("You can buy only " + remainingAllowed + " more ticket(s). Maximum limit is 5.");
        }

        List<Ticket> toSave = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            Ticket ticket = new Ticket();
            ticket.setTicketType(template.getTicketType());
            ticket.setPrice(template.getPrice());
            ticket.setEventId(template.getEventId());
            ticket.setUserId(template.getUserId());
            ticket.setRegistrationId(template.getRegistrationId());
            ticket.setCreatedOn(LocalDate.now());
            ticket.setCreationTime(LocalDateTime.now());
            ticket.setUpdatedOn(LocalDate.now());
            ticket.setDeleted(false);
            toSave.add(ticket);
        }
        return ticketRepository.saveAll(toSave);
    }

    @Override
    public Ticket getTicketById(int id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + id));
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket updateTicket(int id, Ticket updated) {
        Ticket existing = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + id));
        existing.setTicketType(updated.getTicketType());
        existing.setPrice(updated.getPrice());
        existing.setUpdatedOn(LocalDate.now());
        return ticketRepository.save(existing);
    }

    @Override
    public void deleteTicket(int id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + id));
        ticket.setDeleted(true);
        ticket.setDeletedOn(LocalDate.now());
        ticketRepository.save(ticket);
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
