package com.wipro.tickets.tickets.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    // Reference to Event Service (by ID only - no direct FK across services)
    @Column(nullable = false)
    private int eventId;

    // Reference to User (from DEMP service)
    @Column(nullable = false)
    private int userId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    private LocalDateTime bookedAt;

    private LocalDate createdOn;
    private LocalDate updatedOn;
    private LocalDate deletedOn;
    private boolean isDeleted;

    @PrePersist
    void onCreate() {
        this.bookedAt = LocalDateTime.now();
        this.createdOn = LocalDate.now();
        this.isDeleted = false;
    }
}
