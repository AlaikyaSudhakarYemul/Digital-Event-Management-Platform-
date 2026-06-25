package com.wipro.tickets.tickets.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wipro.tickets.tickets.dto.PaymentDTO;
import com.wipro.tickets.tickets.entity.Payment;
import com.wipro.tickets.tickets.entity.PaymentStatus;
import com.wipro.tickets.tickets.entity.Ticket;
import com.wipro.tickets.tickets.entity.TicketStatus;
import com.wipro.tickets.tickets.exception.PaymentNotFoundException;
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

    private Ticket resolveTicketForPayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment payload is required");
        }
        if (payment.getTicket() == null || payment.getTicket().getTicketId() == null) {
            throw new IllegalArgumentException("ticket.ticketId is required");
        }
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        Long ticketId = payment.getTicket().getTicketId();
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        if (ticket.isDeleted()) {
            throw new IllegalStateException("Cannot process payment for a deleted ticket");
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException("Cannot process payment for a cancelled ticket");
        }
        if (!Objects.equals(ticket.getTotalAmount(), payment.getAmount())) {
            throw new IllegalArgumentException("Payment amount must match ticket totalAmount");
        }
        if (paymentRepository.findByTicket_TicketId(ticketId).isPresent()) {
            throw new IllegalStateException("Payment already exists for ticket ID: " + ticketId);
        }

        return ticket;
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
    @Transactional
    public PaymentDTO processPayment(Payment payment) {
        Ticket ticket = resolveTicketForPayment(payment);
        logger.info("Processing payment for ticketId={}", ticket.getTicketId());

        payment.setTicket(ticket);
        payment.setPaidAt(LocalDateTime.now());
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(PaymentStatus.PENDING);
        }
        Payment saved = paymentRepository.save(payment);
        if (PaymentStatus.SUCCESS.equals(saved.getPaymentStatus())) {
            Ticket persistedTicket = saved.getTicket();
            persistedTicket.setStatus(TicketStatus.CONFIRMED);
            persistedTicket.setUpdatedOn(LocalDate.now());
            ticketRepository.save(persistedTicket);
            logger.info("Ticket ID={} confirmed after successful payment", persistedTicket.getTicketId());
        }
        return toDTO(saved);
    }

    @Override
    public PaymentDTO getPaymentById(Long paymentId) {
        logger.info("Fetching payment with ID={}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));
        return toDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByTicketId(Long ticketId) {
        logger.info("Fetching payment for ticketId={}", ticketId);
        Payment payment = paymentRepository.findByTicket_TicketId(ticketId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for ticket ID: " + ticketId));
        return toDTO(payment);
    }

    @Override
    public List<PaymentDTO> getAllPayments() {
        logger.info("Fetching all payments");
        return paymentRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentDTO updatePaymentStatus(Long paymentId, PaymentStatus status) {
        logger.info("Updating payment ID={} status to {}", paymentId, status);
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        if (PaymentStatus.REFUNDED.equals(payment.getPaymentStatus())) {
            throw new IllegalStateException("Refunded payment cannot be changed");
        }

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

    @Override
    @Transactional
    public PaymentDTO refundPayment(Long paymentId) {
        logger.info("Refunding payment with ID={}", paymentId);
        return updatePaymentStatus(paymentId, PaymentStatus.REFUNDED);
    }
}
