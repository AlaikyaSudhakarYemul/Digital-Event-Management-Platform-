package com.wipro.tickets.tickets.service;

import java.util.List;

import com.wipro.tickets.tickets.dto.PaymentDTO;
import com.wipro.tickets.tickets.entity.Payment;
import com.wipro.tickets.tickets.entity.PaymentStatus;

public interface PaymentService {

    PaymentDTO processPayment(Payment payment);

    PaymentDTO getPaymentById(Long paymentId);

    PaymentDTO getPaymentByTicketId(Long ticketId);

    List<PaymentDTO> getAllPayments();

    PaymentDTO updatePaymentStatus(Long paymentId, PaymentStatus status);
}
