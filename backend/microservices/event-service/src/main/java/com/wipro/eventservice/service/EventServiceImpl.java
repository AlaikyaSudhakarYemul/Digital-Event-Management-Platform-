package com.wipro.eventservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wipro.eventservice.entity.*;
import com.wipro.eventservice.exception.AddressNotFoundException;
import com.wipro.eventservice.exception.EventNotFoundException;
import com.wipro.eventservice.repository.EventRepository;
import com.wipro.eventservice.repository.SpeakerRepository;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final AddressService addressService;
    private final SpeakerService speakerService;
    private final SpeakerRepository speakerRepository;

    public EventServiceImpl(EventRepository eventRepository, AddressService addressService,
                            SpeakerService speakerService, SpeakerRepository speakerRepository) {
        this.eventRepository = eventRepository;
        this.addressService = addressService;
        this.speakerService = speakerService;
        this.speakerRepository = speakerRepository;
    }

    @Override
    public Event createEvent(Event event) {
        event.setEventType(event.getEventType());
        event.setCreationTime(LocalDateTime.now());

        if (EventType.VIRTUAL.equals(event.getEventType())) {
            event.setAddress(null);
        } else if (event.getAddress() != null) {
            Address address = addressService.getAddress(event.getAddress().getAddressId());
            event.setAddress(address);
        }

        if (event.getSpeakers() != null && !event.getSpeakers().isEmpty()) {
            List<Integer> speakerIds = event.getSpeakers().stream()
                    .map(Speaker::getSpeakerId)
                    .collect(Collectors.toList());
            List<Speaker> speakers = speakerRepository.findAllById(speakerIds);
            event.setSpeakers(speakers);
        }

        event.setActiveStatus(EventStatus.ACTIVE);
        event.setCreatedOn(LocalDate.now());
        event.setCreationTime(LocalDateTime.now());
        event.setUpdatedOn(LocalDate.now());
        event.setDeleted(false);
        return eventRepository.save(event);
    }

    @Override
    public Event getEventById(int id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
    }

    @Override
    public List<Event> findAllEventsByUserId(Integer userId) {
        List<Event> events = eventRepository.findByUserId(userId);
        if (events == null || events.isEmpty()) {
            throw new EventNotFoundException("No events found for organizer with id: " + userId);
        }
        return events;
    }

    @Override
    public List<Event> getAllEvents() {
        List<Event> events = eventRepository.findAllInReverse();
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found.");
        }

        LocalDate today = LocalDate.now();
        return events.stream()
                .filter(e -> e.getActiveStatus() != EventStatus.DELETED)
                .peek(e -> {
                    if (e.getDate() != null && e.getDate().isBefore(today)
                            && e.getActiveStatus() != EventStatus.COMPLETED) {
                        e.setActiveStatus(EventStatus.COMPLETED);
                        eventRepository.save(e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Event updateEvent(int id, Event updatedEvent) {
        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));

        existing.setEventName(updatedEvent.getEventName());
        existing.setDescription(updatedEvent.getDescription());
        existing.setDate(updatedEvent.getDate());
        existing.setTime(updatedEvent.getTime());
        existing.setEventType(updatedEvent.getEventType());
        existing.setImage(updatedEvent.getImage());
        existing.setMaxAttendees(updatedEvent.getMaxAttendees());

        if (EventType.VIRTUAL.equals(updatedEvent.getEventType())) {
            existing.setAddress(null);
        } else if (updatedEvent.getAddress() != null) {
            Address address = addressService.getAddress(updatedEvent.getAddress().getAddressId());
            existing.setAddress(address);
        }

        if (updatedEvent.getSpeakers() != null && !updatedEvent.getSpeakers().isEmpty()) {
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
        existing.setUpdatedOn(LocalDate.now());
        return eventRepository.save(existing);
    }

    @Override
    public void deleteEvent(int id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        event.setActiveStatus(EventStatus.DELETED);
        event.setUpdatedOn(LocalDate.now());
        eventRepository.save(event);
    }

    @Override
    public List<Event> findByEventName(String eventName) {
        List<Event> events = eventRepository.findByEventName(eventName);
        if (events == null || events.isEmpty()) {
            throw new EventNotFoundException("Event not found with name: " + eventName);
        }
        return events;
    }

    @Override
    public Page<Event> getPaginatedEvents(String eventName, Pageable pageable) {
        EventStatus active = EventStatus.ACTIVE;
        if (eventName != null && !eventName.isBlank()) {
            return eventRepository.findByEventNameContainingIgnoreCaseAndActiveStatusOrderByCreationTimeDesc(
                    eventName, active, pageable);
        }
        return eventRepository.findByActiveStatusOrderByCreationTimeDesc(active, pageable);
    }
}
