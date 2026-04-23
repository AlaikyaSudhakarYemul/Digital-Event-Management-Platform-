package com.wipro.demp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.wipro.demp.entity.PaymentStatus;
import java.math.BigDecimal;

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
    private TicketType ticketType;
    
    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.00", message = "Price must be greater than or equal to 0.00")
    @Digits(integer = 10, fraction = 2, message = "Price must be a valid monetary amount")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    
    private int eventId;
    
    private int userId;
    
    @Column
    private int registrationId;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private LocalDate createdOn;
    private LocalDateTime creationTime;
    private LocalDate updatedOn;
    private LocalDate deletedOn;
    private boolean isDeleted;

    @Transient
    private PaymentStatus paymentStatus;

}