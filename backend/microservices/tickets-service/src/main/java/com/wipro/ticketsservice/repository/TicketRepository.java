package com.wipro.ticketsservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wipro.ticketsservice.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByEventId(int eventId);
    List<Ticket> findByUserId(int userId);
    long countByUserIdAndEventIdAndIsDeletedFalse(int userId, int eventId);
}
