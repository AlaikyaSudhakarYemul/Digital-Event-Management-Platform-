package com.wipro.tickets.tickets.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.wipro.tickets.tickets.dto.TicketDTO;
import com.wipro.tickets.tickets.entity.Ticket;
import com.wipro.tickets.tickets.entity.TicketStatus;
import com.wipro.tickets.tickets.exception.TicketNotFoundException;
import com.wipro.tickets.tickets.repositoty.TicketRepository;

@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final ExternalReferenceValidationService externalReferenceValidationService;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             ExternalReferenceValidationService externalReferenceValidationService) {
        this.ticketRepository = ticketRepository;
        this.externalReferenceValidationService = externalReferenceValidationService;
    }

    private void validateUserReference(int userId, String authorizationHeader) {
        externalReferenceValidationService.validateUserExists(userId, authorizationHeader);
    }

    private void validateEventReference(int eventId) {
        externalReferenceValidationService.validateEventExists(eventId);
    }

    private boolean validStatusTransition(TicketStatus current, TicketStatus target) {
        if (current == target) {
            return true;
        }
        if (current == TicketStatus.RESERVED) {
            return target == TicketStatus.CONFIRMED || target == TicketStatus.CANCELLED;
        }
        if (current == TicketStatus.CONFIRMED) {
            return target == TicketStatus.CANCELLED;
        }
        return false;
    }

    private void validateBookingPayload(Ticket ticket, String authorizationHeader) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket payload is required");
        }
        if (ticket.getUserId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        if (ticket.getEventId() <= 0) {
            throw new IllegalArgumentException("eventId must be greater than 0");
        }
        if (ticket.getQuantity() <= 0) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
        if (ticket.getTotalAmount() == null || ticket.getTotalAmount().signum() <= 0) {
            throw new IllegalArgumentException("totalAmount must be greater than 0");
        }

        validateUserReference(ticket.getUserId(), authorizationHeader);
        validateEventReference(ticket.getEventId());
    }

    private TicketDTO toDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setTicketId(ticket.getTicketId());
        dto.setEventId(ticket.getEventId());
        dto.setUserId(ticket.getUserId());
        dto.setQuantity(ticket.getQuantity());
        dto.setTotalAmount(ticket.getTotalAmount());
        dto.setStatus(ticket.getStatus());
        dto.setBookedAt(ticket.getBookedAt());
        dto.setCreatedOn(ticket.getCreatedOn());
        dto.setUpdatedOn(ticket.getUpdatedOn());
        return dto;
    }

    @Override
    public TicketDTO bookTicket(Ticket ticket, String authorizationHeader) {
        logger.info("Booking ticket for userId={}, eventId={}", ticket.getUserId(), ticket.getEventId());

        validateBookingPayload(ticket, authorizationHeader);

        if (ticket.getTicketId() != null) {
            throw new IllegalArgumentException("ticketId should not be sent while creating a ticket");
        }

        ticket.setStatus(TicketStatus.RESERVED);
        ticket.setDeleted(false);
        Ticket saved = ticketRepository.save(ticket);
        logger.info("Ticket booked with ID={}", saved.getTicketId());
        return toDTO(saved);
    }

    @Override
    public TicketDTO getTicketById(Long ticketId) {
        logger.info("Fetching ticket with ID={}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
        return toDTO(ticket);
    }

    @Override
    public List<TicketDTO> getAllTickets() {
        logger.info("Fetching all tickets");
        return ticketRepository.findByIsDeletedFalse().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsByUserId(int userId) {
        logger.info("Fetching tickets for userId={}", userId);
        return ticketRepository.findByUserId(userId).stream()
                .filter(ticket -> !ticket.isDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsByEventId(int eventId) {
        logger.info("Fetching tickets for eventId={}", eventId);
        return ticketRepository.findByEventId(eventId).stream()
                .filter(ticket -> !ticket.isDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TicketDTO cancelTicket(Long ticketId) {
        logger.info("Cancelling ticket with ID={}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
        if (ticket.isDeleted()) {
            throw new IllegalStateException("Cannot cancel a deleted ticket");
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalArgumentException("Ticket already cancelled");
        }
        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setUpdatedOn(LocalDate.now());
        return toDTO(ticketRepository.save(ticket));
    }

    @Override
    public TicketDTO updateTicketStatus(Long ticketId, TicketStatus status) {
        logger.info("Updating ticket ID={} status to {}", ticketId, status);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        if (ticket.isDeleted()) {
            throw new IllegalStateException("Cannot update status for a deleted ticket");
        }
        if (!validStatusTransition(ticket.getStatus(), status)) {
            throw new IllegalArgumentException("Invalid status transition from " + ticket.getStatus() + " to " + status);
        }
        ticket.setStatus(status);
        ticket.setUpdatedOn(LocalDate.now());
        return toDTO(ticketRepository.save(ticket));
    }

    @Override
    public List<TicketDTO> getActiveTickets() {
        logger.info("Fetching active (non-deleted) tickets");
        return ticketRepository.findByIsDeletedFalse().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void deleteTicket(Long ticketId) {
        logger.info("Soft-deleting ticket with ID={}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
        if (ticket.isDeleted()) {
            return;
        }
        ticket.setDeleted(true);
        ticket.setDeletedOn(LocalDate.now());
        ticket.setUpdatedOn(LocalDate.now());
        ticketRepository.save(ticket);
    }
}
