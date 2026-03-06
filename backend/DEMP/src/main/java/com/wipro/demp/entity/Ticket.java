package com.wipro.demp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ticketId;

    private TicketType ticketType;
    
    private double price;
    
    private int eventId;
    
    private int userId;
    
    private int registrationId;

    private LocalDate createdOn;
    private LocalDateTime creationTime;
    private LocalDate updatedOn;
    private LocalDate deletedOn;
    private boolean isDeleted;

}