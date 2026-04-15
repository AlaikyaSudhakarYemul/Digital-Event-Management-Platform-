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

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
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
    public TicketDTO bookTicket(Ticket ticket) {
        logger.info("Booking ticket for userId={}, eventId={}", ticket.getUserId(), ticket.getEventId());
        ticket.setStatus(TicketStatus.RESERVED);
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
        return ticketRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsByUserId(int userId) {
        logger.info("Fetching tickets for userId={}", userId);
        return ticketRepository.findByUserId(userId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsByEventId(int eventId) {
        logger.info("Fetching tickets for eventId={}", eventId);
        return ticketRepository.findByEventId(eventId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public TicketDTO cancelTicket(Long ticketId) {
        logger.info("Cancelling ticket with ID={}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setUpdatedOn(LocalDate.now());
        return toDTO(ticketRepository.save(ticket));
    }

    @Override
    public void deleteTicket(Long ticketId) {
        logger.info("Soft-deleting ticket with ID={}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
        ticket.setDeleted(true);
        ticket.setDeletedOn(LocalDate.now());
        ticketRepository.save(ticket);
    }
}
