package com.wipro.tickets.tickets.repositoty;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wipro.tickets.tickets.entity.Payment;
import com.wipro.tickets.tickets.entity.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTicket_TicketId(Long ticketId);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
}
