package com.wipro.demp.repository;


import com.wipro.demp.entity.Payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentsRepository extends JpaRepository<Payment, Long> {
  Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
}

