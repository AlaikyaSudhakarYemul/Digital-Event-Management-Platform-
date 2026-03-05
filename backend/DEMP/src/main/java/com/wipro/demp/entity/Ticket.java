package com.wipro.demp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {

    private int ticketId;
    private TicketType ticketType;
    private double price;
    private int eventId;
    private int registrationId;

}
