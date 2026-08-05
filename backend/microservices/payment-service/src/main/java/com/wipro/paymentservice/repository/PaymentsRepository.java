package com.wipro.paymentservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wipro.paymentservice.entity.Payment;

public interface PaymentsRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findTopByRegistrationIdOrderByIdDesc(Long registrationId);
}
