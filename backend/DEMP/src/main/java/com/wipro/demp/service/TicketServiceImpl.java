package com.wipro.demp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.Ticket;
import com.wipro.demp.entity.Users;
import com.wipro.demp.repository.EventRepository;
import com.wipro.demp.repository.TicketRepository;
import com.wipro.demp.repository.UserRepository;

@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendBaseUrl;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserRepository userRepository,
                             EventRepository eventRepository,
                             JavaMailSender mailSender) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.mailSender = mailSender;
    }

    @Override
    public Ticket createTicket(Ticket ticket) {

        ticket.setCreatedOn(LocalDate.now());
        ticket.setCreationTime(LocalDateTime.now());
        ticket.setUpdatedOn(LocalDate.now());
        ticket.setDeleted(false);

        Ticket savedTicket = ticketRepository.save(ticket);

        // Send a second mail specifically for ticket access after successful ticket creation.
        try {
            sendTicketDownloadMail(savedTicket);
        } catch (Exception e) {
            log.error("Failed to send ticket download email for ticket id {}: {}", savedTicket.getTicketId(), e.getMessage(), e);
        }

        return savedTicket;
    }

    private void sendTicketDownloadMail(Ticket ticket) {
        Users user = userRepository.findById(ticket.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found for ticket email"));

        Event event = eventRepository.findById(ticket.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found for ticket email"));

        String apiTicketLink = backendBaseUrl + "/api/tickets/" + ticket.getTicketId();
        String dashboardLink = frontendBaseUrl + "/userdashboard";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Thanks for registering - Download your event ticket");
        message.setText(
                "Hi " + safe(user.getUserName()) + ",\n\n"
                        + "Thanks for registering for the event \"" + safe(event.getEventName()) + "\".\n"
                        + "Your ticket has been generated successfully.\n\n"
                        + "Ticket ID: " + ticket.getTicketId() + "\n"
                        + "Ticket Type: " + ticket.getTicketType() + "\n"
                        + "Ticket Price: " + ticket.getPrice() + "\n\n"
                        + "Please click here to download/view your ticket:\n"
                        + apiTicketLink + "\n\n"
                        + "Or login and download from your dashboard:\n"
                        + dashboardLink + "\n\n"
                        + "Thank you,\n"
                        + "EVENTRA Team"
        );

        mailSender.send(message);
        log.info("Ticket download email sent to {} for ticket {}", user.getEmail(), ticket.getTicketId());
    }

    private static String safe(String value) {
        return value == null ? "" : value;
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
            return ticketOpt.get();
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
        return tickets;

    }

    @Override
    public List<Ticket> getTicketsByEventId(int eventId) {
       
            List<Ticket> tickets = ticketRepository.findByEventId(eventId);
            if (tickets.isEmpty()) {
                throw new RuntimeException("No tickets found for event id: " + eventId);
            }
            return tickets;
    }

    @Override
    public List<Ticket> getTicketsByUserId(int userId) {
        
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        if (tickets.isEmpty()) {
            throw new RuntimeException("No tickets found for user id: " + userId);
        }
        return tickets;
    }

}
