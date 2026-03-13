package com.wipro.demp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wipro.demp.entity.Ticket;
import com.wipro.demp.entity.Payment;
import com.wipro.demp.entity.PaymentStatus;
import com.wipro.demp.repository.TicketRepository;
import com.wipro.demp.repository.PaymentsRepository;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final PaymentsRepository paymentsRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, PaymentsRepository paymentsRepository) {
        this.ticketRepository = ticketRepository;
        this.paymentsRepository = paymentsRepository;
    }

    @Override
    public Ticket createTicket(Ticket ticket) {

        ticket.setCreatedOn(LocalDate.now());
        ticket.setCreationTime(LocalDateTime.now());
        ticket.setUpdatedOn(LocalDate.now());
        ticket.setDeleted(false);

        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket updateTicket(int id, Ticket ticket) {
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
       
        if (ticketRepository.findById(id).isEmpty()) {
            throw new RuntimeException("Ticket not found with id: " + id);
        }
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            Ticket t = ticketOpt.get();
            populatePaymentStatus(t);
            return t;
        } else {
            throw new RuntimeException("Ticket not found with id: " + id);
        }
    }

    @Override
    public List<Ticket> getAllTickets() {
       
        List<Ticket> tickets = ticketRepository.findAll();
        if (tickets.isEmpty()) {
            throw new RuntimeException("No tickets found.");
        }
        tickets.forEach(this::populatePaymentStatus);
        return tickets;

    }

    @Override
    public List<Ticket> getTicketsByEventId(int eventId) {
       
            List<Ticket> tickets = ticketRepository.findByEventId(eventId);
            if (tickets.isEmpty()) {
                throw new RuntimeException("No tickets found for event id: " + eventId);
            }
            tickets.forEach(this::populatePaymentStatus);
            return tickets;
    }

    @Override
    public List<Ticket> getTicketsByUserId(int userId) {
        
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        if (tickets.isEmpty()) {
            throw new RuntimeException("No tickets found for user id: " + userId);
        }
        tickets.forEach(this::populatePaymentStatus);
        return tickets;
    }

    private void populatePaymentStatus(Ticket ticket) {
        try {
            if (ticket == null) return;
            // registrationId is stored as int on Ticket, payments use Long registrationId
            Long regId = ticket.getRegistrationId() == 0 ? null : Long.valueOf(ticket.getRegistrationId());
            if (regId == null) {
                ticket.setPaymentStatus(null);
                return;
            }
            Optional<Payment> p = paymentsRepository.findTopByRegistrationIdOrderByIdDesc(regId);
            if (p.isPresent()) {
                ticket.setPaymentStatus(p.get().getStatus());
            } else {
                ticket.setPaymentStatus(null);
            }
        } catch (Exception ex) {
            // don't fail requests due to payment lookup issues
            ticket.setPaymentStatus(null);
        }
    }

}
