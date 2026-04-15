package com.wipro.tickets.tickets.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.tickets.tickets.dto.PaymentDTO;
import com.wipro.tickets.tickets.entity.Payment;
import com.wipro.tickets.tickets.entity.PaymentStatus;
import com.wipro.tickets.tickets.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody Payment payment) {
        logger.info("Processing payment for ticketId={}", payment.getTicket().getTicketId());
        PaymentDTO processed = paymentService.processPayment(payment);
        return new ResponseEntity<>(processed, HttpStatus.CREATED);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long paymentId) {
        logger.info("Fetching payment with ID={}", paymentId);
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getPaymentByTicketId(@PathVariable Long ticketId) {
        logger.info("Fetching payment for ticketId={}", ticketId);
        return ResponseEntity.ok(paymentService.getPaymentByTicketId(ticketId));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPayments() {
        logger.info("Fetching all payments");
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @PutMapping("/{paymentId}/status/{status}")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long paymentId, @PathVariable PaymentStatus status) {
        logger.info("Updating payment ID={} to status={}", paymentId, status);
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
    }
}
