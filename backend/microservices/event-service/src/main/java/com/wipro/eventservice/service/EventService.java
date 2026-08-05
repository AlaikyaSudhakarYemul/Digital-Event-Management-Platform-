package com.wipro.eventservice.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wipro.eventservice.entity.Event;

public interface EventService {
    Event createEvent(Event event);
    Event getEventById(int id);
    List<Event> getAllEvents();
    Event updateEvent(int id, Event event);
    void deleteEvent(int id);
    List<Event> findByEventName(String eventName);
    Page<Event> getPaginatedEvents(String eventName, Pageable pageable);
    List<Event> findAllEventsByUserId(Integer userId);
}
