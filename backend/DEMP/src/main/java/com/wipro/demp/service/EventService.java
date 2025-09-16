package com.wipro.demp.service;

 
import com.wipro.demp.entity.Event;
import java.util.List;
 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 
public interface EventService {
      Event createEvent(Event event);
      Event getEventById(int id);
      List<Event> getAllEvents();
      Event updateEvent(int id, Event updatedEvent);
      void deleteEvent (int id);
      List<Event> findByEventName(String eventName);
      Page<Event> getPaginatedEvents(String eventName, Pageable pageable);
}