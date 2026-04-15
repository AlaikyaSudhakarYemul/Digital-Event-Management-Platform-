package com.wipro.tickets.tickets.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.wipro.tickets.tickets.entity.PaymentStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDTO {
    private Long paymentId;
    private Long ticketId;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDate createdOn;
    private LocalDate updatedOn;
}
