package com.wipro.tickets.tickets.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.wipro.tickets.tickets.entity.TicketStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketDTO {
    private Long ticketId;
    private int eventId;
    private int userId;
    private int quantity;
    private BigDecimal totalAmount;
    private TicketStatus status;
    private LocalDateTime bookedAt;
    private LocalDate createdOn;
    private LocalDate updatedOn;
}
