package com.wipro.event.repository;

import java.util.List;
 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.wipro.event.entity.Event;
import com.wipro.event.entity.EventStatus;
 
@Repository
public interface EventRepository extends JpaRepository <Event, Integer> {
    
    List<Event> findByEventName(String eventName);
 
    Page<Event> findAll(Pageable pageable);
 
    Page<Event> findByEventNameContainingIgnoreCase(String eventName, Pageable pageable);
 
    // List<Event> findByUser(Users user);
 
    @Query("SELECT e FROM Event e ORDER BY e.id DESC")
    List<Event> findAllInReverse();
    
    Page<Event> findByActiveStatus(EventStatus status, Pageable pageable);
    Page<Event> findByActiveStatusOrderByCreationTimeDesc(EventStatus status, Pageable pageable);
    Page<Event> findByEventNameContainingIgnoreCaseAndActiveStatusOrderByCreationTimeDesc(String eventName, EventStatus status, Pageable pageable);
 
    Page<Event> findByEventNameContainingIgnoreCaseAndActiveStatus(String eventName, EventStatus status, Pageable pageable);
 
    List<Event> findByUserId(Integer userId);
}
