package com.wipro.ticketsservice.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ticket_gen")
    @TableGenerator(
            name = "ticket_gen",
            table = "id_generator",
            pkColumnName = "gen_name",
            valueColumnName = "gen_val",
            pkColumnValue = "ticket_id",
            initialValue = 10000,
            allocationSize = 1
    )
    private int ticketId;

    @NotNull(message = "Ticket type cannot be null")
    @Enumerated(EnumType.STRING)
    private TicketType ticketType;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.00", message = "Price must be >= 0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    private int eventId;
    private int userId;
    private int registrationId;

    private LocalDate createdOn;
    private LocalDateTime creationTime;
    private LocalDate updatedOn;
    private LocalDate deletedOn;
    private boolean isDeleted;
}
