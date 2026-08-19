package com.wipro.ticketsservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.wipro.ticketsservice.client.EventServiceClient;
import com.wipro.ticketsservice.client.NotificationServiceClient;
import com.wipro.ticketsservice.client.PaymentServiceClient;
import com.wipro.ticketsservice.client.UserServiceClient;
import com.wipro.ticketsservice.entity.PaymentStatus;
import com.wipro.ticketsservice.entity.Ticket;
import com.wipro.ticketsservice.exception.TicketNotFoundException;
import com.wipro.ticketsservice.repository.TicketRepository;

@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);
    private static final int MAX_TICKETS_PER_USER_PER_EVENT = 5;

    private final TicketRepository ticketRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public TicketServiceImpl(TicketRepository ticketRepository,
                              PaymentServiceClient paymentServiceClient,
                              UserServiceClient userServiceClient,
                              EventServiceClient eventServiceClient,
                              NotificationServiceClient notificationServiceClient) {
        this.ticketRepository = ticketRepository;
        this.paymentServiceClient = paymentServiceClient;
        this.userServiceClient = userServiceClient;
        this.eventServiceClient = eventServiceClient;
        this.notificationServiceClient = notificationServiceClient;
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

        List<Ticket> savedTickets = ticketRepository.saveAll(toSave);

        // Send ticket confirmation emails best-effort without failing ticket creation.
        savedTickets.forEach(this::sendTicketConfirmationNotification);

        return savedTickets;
    }

    private void sendTicketConfirmationNotification(Ticket ticket) {
        try {
            Map<String, Object> user = userServiceClient.getUserById(ticket.getUserId());
            Map<String, Object> event = eventServiceClient.getEventById(ticket.getEventId());

            Map<String, Object> notification = new HashMap<>();
            notification.put("toEmail", user == null ? null : user.get("email"));
            notification.put("userName", user == null ? null : user.get("userName"));
            notification.put("eventName", event == null ? null : event.get("eventName"));
            notification.put("eventDate", event == null ? null : event.get("date"));
            notification.put("registrationId", ticket.getRegistrationId());

            notificationServiceClient.sendTicketConfirmation(notification);
            log.info("Ticket confirmation notification requested for ticket id {}", ticket.getTicketId());
        } catch (Exception e) {
            log.error("Failed to send ticket confirmation notification for ticket id {}: {}",
                    ticket.getTicketId(), e.getMessage(), e);
        }
    }

    private void populatePaymentStatus(Ticket ticket) {
        if (ticket.getRegistrationId() <= 0) {
            return;
        }
        try {
            Map<String, Object> payment = paymentServiceClient.getPaymentStatus(ticket.getRegistrationId());
            if (payment != null && payment.get("status") != null) {
                ticket.setPaymentStatus(PaymentStatus.valueOf(payment.get("status").toString()));
            }
        } catch (Exception e) {
            log.warn("Could not fetch payment status for registration {}: {}",
                    ticket.getRegistrationId(), e.getMessage());
        }
    }

    @Override
    public Ticket getTicketById(int id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + id));
        populatePaymentStatus(ticket);
        return ticket;
    }

    @Override
    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        tickets.forEach(this::populatePaymentStatus);
        return tickets;
    }

    @Override
    public Ticket updateTicket(int id, Ticket updated) {
        Ticket existing = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + id));
        existing.setEventId(updated.getEventId());
        existing.setUserId(updated.getUserId());
        existing.setRegistrationId(updated.getRegistrationId());
        existing.setPrice(updated.getPrice());
        existing.setUpdatedOn(LocalDate.now());
        return ticketRepository.save(existing);
    }

    @Override
    public void deleteTicket(int id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + id));
        ticket.setDeleted(true);
        ticket.setDeletedOn(LocalDate.now());
        ticketRepository.save(ticket);
    }

    @Override
    public List<Ticket> getTicketsByEventId(int eventId) {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        tickets.forEach(this::populatePaymentStatus);
        return tickets;
    }

    @Override
    public List<Ticket> getTicketsByUserId(int userId) {
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        tickets.forEach(this::populatePaymentStatus);
        return tickets;
    }
}
