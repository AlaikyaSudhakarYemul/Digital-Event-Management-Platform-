package com.wipro.tickets.tickets.repositoty;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wipro.tickets.tickets.entity.Ticket;
import com.wipro.tickets.tickets.entity.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUserId(int userId);

    List<Ticket> findByEventId(int eventId);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByUserIdAndEventId(int userId, int eventId);

    List<Ticket> findByIsDeletedFalse();
}
