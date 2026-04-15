package com.wipro.tickets.tickets.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.wipro.tickets.tickets.dto.PaymentDTO;
import com.wipro.tickets.tickets.entity.Payment;
import com.wipro.tickets.tickets.entity.PaymentStatus;
import com.wipro.tickets.tickets.entity.Ticket;
import com.wipro.tickets.tickets.entity.TicketStatus;
import com.wipro.tickets.tickets.exception.TicketNotFoundException;
import com.wipro.tickets.tickets.repositoty.PaymentRepository;
import com.wipro.tickets.tickets.repositoty.TicketRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, TicketRepository ticketRepository) {
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
    }

    private PaymentDTO toDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setTicketId(payment.getTicket().getTicketId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaidAt(payment.getPaidAt());
        dto.setCreatedOn(payment.getCreatedOn());
        dto.setUpdatedOn(payment.getUpdatedOn());
        return dto;
    }

    @Override
    public PaymentDTO processPayment(Payment payment) {
        logger.info("Processing payment for ticketId={}", payment.getTicket().getTicketId());
        payment.setPaidAt(LocalDateTime.now());
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(PaymentStatus.PENDING);
        }
        Payment saved = paymentRepository.save(payment);
        if (PaymentStatus.SUCCESS.equals(saved.getPaymentStatus())) {
            Ticket ticket = saved.getTicket();
            ticket.setStatus(TicketStatus.CONFIRMED);
            ticket.setUpdatedOn(LocalDate.now());
            ticketRepository.save(ticket);
            logger.info("Ticket ID={} confirmed after successful payment", ticket.getTicketId());
        }
        return toDTO(saved);
    }

    @Override
    public PaymentDTO getPaymentById(Long paymentId) {
        logger.info("Fetching payment with ID={}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new TicketNotFoundException("Payment not found with ID: " + paymentId));
        return toDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByTicketId(Long ticketId) {
        logger.info("Fetching payment for ticketId={}", ticketId);
        Payment payment = paymentRepository.findByTicket_TicketId(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Payment not found for ticket ID: " + ticketId));
        return toDTO(payment);
    }

    @Override
    public List<PaymentDTO> getAllPayments() {
        logger.info("Fetching all payments");
        return paymentRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public PaymentDTO updatePaymentStatus(Long paymentId, PaymentStatus status) {
        logger.info("Updating payment ID={} status to {}", paymentId, status);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new TicketNotFoundException("Payment not found with ID: " + paymentId));
        payment.setPaymentStatus(status);
        payment.setUpdatedOn(LocalDate.now());
        if (PaymentStatus.SUCCESS.equals(status)) {
            payment.setPaidAt(LocalDateTime.now());
            Ticket ticket = payment.getTicket();
            ticket.setStatus(TicketStatus.CONFIRMED);
            ticket.setUpdatedOn(LocalDate.now());
            ticketRepository.save(ticket);
        } else if (PaymentStatus.REFUNDED.equals(status)) {
            Ticket ticket = payment.getTicket();
            ticket.setStatus(TicketStatus.CANCELLED);
            ticket.setUpdatedOn(LocalDate.now());
            ticketRepository.save(ticket);
        }
        return toDTO(paymentRepository.save(payment));
    }
}
