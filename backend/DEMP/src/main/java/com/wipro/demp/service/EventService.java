package com.wipro.demp.service;

import com.wipro.demp.dto.EventDTO;
import com.wipro.demp.entity.Event;
import java.util.List;
 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 
public interface EventService {
      EventDTO createEvent(Event event);
      EventDTO getEventById(int id);
      List<EventDTO> getAllEvents();
      EventDTO updateEvent(int id, Event updatedEvent);
      void deleteEvent (int id);
      List<EventDTO> findByEventName(String eventName);
      Page<EventDTO> getPaginatedEvents(String eventName, Pageable pageable);
}