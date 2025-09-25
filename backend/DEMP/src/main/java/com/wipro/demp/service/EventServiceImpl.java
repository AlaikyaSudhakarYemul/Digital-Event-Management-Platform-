package com.wipro.demp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wipro.demp.entity.*;
import com.wipro.demp.repository.*;
import com.wipro.demp.exception.*;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final SpeakerService speakerService;
    private final SpeakerRepository speakerRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository,
            SpeakerService speakerService, SpeakerRepository speakerRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.speakerService = speakerService;
        this.speakerRepository = speakerRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Event createEvent(Event event) {
        event.setEventType(event.getEventType());
        event.setCreationTime(LocalDateTime.now());
        // For microservice: just set addressId, do not fetch Address or set Address object
        if (EventType.VIRTUAL.equals(event.getEventType())) {
            event.setAddressId(null);
        }

        if (event.getSpeakers() != null && !event.getSpeakers().isEmpty()) {
            List<Integer> speakerIds = event.getSpeakers().stream()
                    .map(Speaker::getSpeakerId)
                    .collect(Collectors.toList());
            List<Speaker> speakers = speakerRepository.findAllById(speakerIds);
            event.setSpeakers(speakers);
        }

        Users createdBy = userRepository.findById(event.getUser().getUserId())
                .orElseThrow(
                        () -> new EventNotFoundException("User not found with id: " + event.getUser().getUserId()));

        event.setUser(createdBy);

        event.setActiveStatus(EventStatus.ACTIVE);
        event.setMaxAttendees(event.getMaxAttendees());
        event.setCreatedOn(LocalDateTime.now().toLocalDate());
        event.setCreationTime(LocalDateTime.now());
        event.setUpdatedOn(LocalDateTime.now().toLocalDate());
        event.setDeleted(false);
        return eventRepository.save(event);
    }

    @Override
    public Event getEventById(int id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
    }

    @Override
    public List<Event> getAllEvents() {
        List<Event> events = eventRepository.findAllInReverse();
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found.");
        }

        LocalDate today = LocalDate.now();

        List<Event> upcomingEvents = events.stream()
                .filter(event -> {
                    if (event.getDate() != null && event.getDate().isBefore(today)) {
                        event.setActiveStatus(EventStatus.COMPLETED);
                        eventRepository.save(event);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return upcomingEvents;
    }

    @Override
    public Event updateEvent(int id, Event updatedEvent) {
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException("Event not found with id: " + id);
        }

        Event existing = getEventById(id);
        existing.setEventName(updatedEvent.getEventName());
        existing.setDescription(updatedEvent.getDescription());
        existing.setDate(updatedEvent.getDate());
        existing.setEventType(updatedEvent.getEventType());
        existing.setImage(updatedEvent.getImage());
        existing.setMaxAttendees(updatedEvent.getMaxAttendees());
        // For microservice: just set addressId, do not fetch Address or set Address object
        if (EventType.VIRTUAL.equals(updatedEvent.getEventType())) {
            existing.setAddressId(null);
        } else {
            existing.setAddressId(updatedEvent.getAddressId());
        }

        if (updatedEvent.getSpeakers() != null &&
                !updatedEvent.getSpeakers().isEmpty()) {
            List<Integer> speakerIds = updatedEvent.getSpeakers().stream()
                    .map(Speaker::getSpeakerId)
                    .toList();
            List<Speaker> speakers = speakerService.findAllByIds(speakerIds);
            if (speakers.size() != speakerIds.size()) {
                throw new IllegalStateException("One or more speaker IDs are invalid.");
            }
            existing.setSpeakers(speakers);
        } else {
            existing.setSpeakers(List.of());
        }
        existing.setActiveStatus(updatedEvent.getActiveStatus());

        existing.setUpdatedOn(LocalDateTime.now().toLocalDate());
        return eventRepository.save(existing);

    }

    @Override
    public void deleteEvent(int id) {
        if (eventRepository.existsById(id) == false) {
            throw new EventNotFoundException("Event not found with id: " + id);
        }
        Optional<Event> event = eventRepository.findById(id);
        if (event.isPresent()) {
            Event existingEvent = event.get();
            existingEvent.setActiveStatus(EventStatus.DELETED);
            existingEvent.setUpdatedOn(LocalDateTime.now().toLocalDate());
            existingEvent.setCreationTime(LocalDateTime.now());
            eventRepository.save(existingEvent);
        }
    }

    @Override
    public List<Event> findByEventName(String eventName) {
        List<Event> event = eventRepository.findByEventName(eventName);
        if (event == null) {
            throw new EventNotFoundException("Event not found with name: " + eventName);
        }
        return event;
    }

    @Override
    public Page<Event> getPaginatedEvents(String eventName, Pageable pageable) {
        EventStatus active = EventStatus.ACTIVE;

        if (eventName != null && !eventName.isEmpty()) {
            return eventRepository.findByEventNameContainingIgnoreCaseAndActiveStatusOrderByCreationTimeDesc(eventName, active, pageable);
        }

        return eventRepository.findByActiveStatusOrderByCreationTimeDesc(active, pageable);
    }

}
