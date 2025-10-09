package com.wipro.event.service;

import java.util.List;
 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wipro.event.dto.EventDTO;
import com.wipro.event.entity.Event;
 
public interface EventService {
      EventDTO createEvent(Event event);
      EventDTO getEventById(int id);
      List<EventDTO> getAllEvents();
      EventDTO updateEvent(int id, Event updatedEvent);
      void deleteEvent (int id);
      List<EventDTO> findByEventName(String eventName);
      List<EventDTO> findEventsByUserId(int userId);
      Page<EventDTO> getPaginatedEvents(String eventName, Pageable pageable);
}