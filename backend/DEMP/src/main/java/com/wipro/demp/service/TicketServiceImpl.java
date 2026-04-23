package com.wipro.demp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.wipro.demp.repository.PaymentsRepository;
import com.wipro.demp.entity.PaymentStatus;

@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);
    private static final int MAX_TICKETS_PER_USER_PER_EVENT = 5;

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final JavaMailSender mailSender;
    private final PaymentsRepository paymentsRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendBaseUrl;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserRepository userRepository,
                             EventRepository eventRepository,
                             JavaMailSender mailSender,
                             PaymentsRepository paymentsRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.mailSender = mailSender;
        this.paymentsRepository = paymentsRepository;
    }

    @Override
    public Ticket createTicket(Ticket ticket) {
        List<Ticket> created = createMultipleTickets(ticket, 1);
        return created.get(0);
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
            throw new IllegalArgumentException("You can buy only " + remainingAllowed + " more ticket(s) for this event. Maximum limit is 5.");
        }

        List<Ticket> toSave = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            Ticket ticket = new Ticket();
            ticket.setTicketType(ticketTemplate.getTicketType());
            ticket.setPrice(ticketTemplate.getPrice());
            ticket.setEventId(ticketTemplate.getEventId());
            ticket.setUserId(ticketTemplate.getUserId());
            ticket.setRegistrationId(ticketTemplate.getRegistrationId());
            ticket.setQuantity(1);
            ticket.setTotalAmount(ticketTemplate.getPrice());
            ticket.setCreatedOn(LocalDate.now());
            ticket.setCreationTime(LocalDateTime.now());
            ticket.setUpdatedOn(LocalDate.now());
            ticket.setDeleted(false);
            toSave.add(ticket);
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(toSave);

        // Send ticket emails best-effort without failing ticket creation.
        savedTickets.forEach(savedTicket -> {
            try {
                sendTicketDownloadMail(savedTicket);
            } catch (Exception e) {
                log.error("Failed to send ticket download email for ticket id {}: {}", savedTicket.getTicketId(), e.getMessage(), e);
            }
        });

        return savedTickets;
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
        existingTicket.setTotalAmount(ticket.getPrice());
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
            // populate latest payment status if available
            if (t.getRegistrationId() > 0) {
                paymentsRepository.findTopByRegistrationIdOrderByIdDesc((long) t.getRegistrationId())
                    .ifPresent(p -> t.setPaymentStatus(p.getStatus()));
            }
            return t;
        } else {
            throw new RuntimeException("Ticket not found with id: " + id);
        }
    }

    @Override
    public List<Ticket> getAllTickets() {
       
        List<Ticket> tickets = ticketRepository.findAll();
        if (tickets.isEmpty()) {
            return tickets;
        }
        // populate payment status for each ticket
        tickets.forEach(t -> {
            if (t.getRegistrationId() > 0) {
                paymentsRepository.findTopByRegistrationIdOrderByIdDesc((long) t.getRegistrationId())
                    .ifPresent(p -> t.setPaymentStatus(p.getStatus()));
            }
        });
        return tickets;

    }

    @Override
    public List<Ticket> getTicketsByEventId(int eventId) {
       
            List<Ticket> tickets = ticketRepository.findByEventId(eventId);
            if (tickets.isEmpty()) {
                return tickets;
            }
            tickets.forEach(t -> {
                if (t.getRegistrationId() > 0) {
                    paymentsRepository.findTopByRegistrationIdOrderByIdDesc((long) t.getRegistrationId())
                        .ifPresent(p -> t.setPaymentStatus(p.getStatus()));
                }
            });
            return tickets;
    }

    @Override
    public List<Ticket> getTicketsByUserId(int userId) {
        
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        if (tickets.isEmpty()) {
            return tickets;
        }
        tickets.forEach(t -> {
            if (t.getRegistrationId() > 0) {
                paymentsRepository.findTopByRegistrationIdOrderByIdDesc((long) t.getRegistrationId())
                    .ifPresent(p -> t.setPaymentStatus(p.getStatus()));
            }
        });
        return tickets;
    }

}
