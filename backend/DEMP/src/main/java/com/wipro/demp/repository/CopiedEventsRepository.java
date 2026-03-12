package com.wipro.demp.repository;


import java.util.List;
 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.wipro.demp.entity.CopiedEvents;
import com.wipro.demp.entity.EventStatus;
import com.wipro.demp.entity.Users;
 
@Repository
public interface CopiedEventsRepository extends JpaRepository <CopiedEvents, Integer> {
    
    List<CopiedEvents> findByEventName(String copiedEventName);
 
    Page<CopiedEvents> findByEventNameContainingIgnoreCase(String copiedEventName, Pageable pageable);
 
    List<CopiedEvents> findByUser(Users user);
 
    @Query("SELECT e FROM CopiedEvents e ORDER BY e.copiedEventId DESC")
    List<CopiedEvents> findAllInReverse();
 
    Page<CopiedEvents> findByActiveStatus(EventStatus status, Pageable pageable);
    Page<CopiedEvents> findByActiveStatusOrderByCreationTimeDesc(EventStatus status, Pageable pageable);
    Page<CopiedEvents> findByEventNameContainingIgnoreCaseAndActiveStatusOrderByCreationTimeDesc(String eventName, EventStatus status, Pageable pageable);
 
    Page<CopiedEvents> findByEventNameContainingIgnoreCaseAndActiveStatus(String eventName, EventStatus status, Pageable pageable);
 
}
 