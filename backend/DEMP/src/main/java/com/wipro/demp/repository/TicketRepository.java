package com.wipro.demp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wipro.demp.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    public List<Ticket> findByEventId(int eventId);
    
}