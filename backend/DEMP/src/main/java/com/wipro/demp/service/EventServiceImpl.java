package com.wipro.demp.service;

import com.wipro.demp.dto.EventDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.wipro.demp.entity.*;

import com.wipro.demp.repository.*;
import com.wipro.demp.exception.*;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public Address getAddressById(int addressId) {
        String url = "http://localhost:8081/api/admin/" + addressId;
        return restTemplate.getForObject(url, Address.class);
    }

    public Speaker getSpeakerById(int speakerId) {
        String url = "http://localhost:8081/api/speakers/" + speakerId;
        return restTemplate.getForObject(url, Speaker.class);
    }

    public List<Speaker> getAllSpeakers() {
        String url = "http://localhost:8081/api/speakers";
        Speaker[] speakersArray = restTemplate.getForObject(url, Speaker[].class);
        if (speakersArray == null) {
            return new ArrayList<>();
        }
        return List.of(speakersArray);
    }

    public EventDTO getDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setEventId(event.getEventId());
        dto.setEventName(event.getEventName());
        dto.setDescription(event.getDescription());
        dto.setDate(event.getDate());
        dto.setImage(event.getImage());
        dto.setEventType(event.getEventType());
        dto.setAddressId(event.getAddressId());
        if (event.getAddressId() != null) {
            Address address = getAddressById(event.getAddressId());
            dto.setAddress(address);
        }
        dto.setSpeakerIds(event.getSpeakerIds());
        if(event.getSpeakerIds() != null && !event.getSpeakerIds().isEmpty()){
            List<Speaker> speakers = new ArrayList<>();
            for(Integer speakerId : event.getSpeakerIds()){
                Speaker speaker = getSpeakerById(speakerId);
                speakers.add(speaker);
            }
            dto.setSpeakers(speakers);
        }
        dto.setUser(event.getUser());
        dto.setActiveStatus(event.getActiveStatus());
        dto.setMaxAttendees(event.getMaxAttendees());
        dto.setCurrentAttendees(event.getCurrentAttendees());
        dto.setCreationTime(event.getCreationTime());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setUpdatedOn(event.getUpdatedOn());
        dto.setDeletedOn(event.getDeletedOn());
        dto.setDeleted(event.isDeleted());
        dto.setVersion(event.getVersion());
        return dto;
    }

    public Event setDTO(EventDTO dto) {
        Event event = new Event();
        event.setEventId(dto.getEventId());
        event.setEventName(dto.getEventName());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setImage(dto.getImage());
        event.setEventType(dto.getEventType());
        event.setAddressId(dto.getAddressId());
        event.setSpeakerIds(dto.getSpeakerIds());
        event.setUser(dto.getUser());
        event.setActiveStatus(dto.getActiveStatus());
        event.setMaxAttendees(dto.getMaxAttendees());
        event.setCurrentAttendees(dto.getCurrentAttendees());
        event.setCreationTime(dto.getCreationTime());
        event.setCreatedOn(dto.getCreatedOn());
        event.setUpdatedOn(dto.getUpdatedOn());
        event.setDeletedOn(dto.getDeletedOn());
        event.setDeleted(dto.isDeleted());
        event.setVersion(dto.getVersion());
        return event;
    }

    @Override
    public EventDTO createEvent(Event event) {

        event.setEventType(event.getEventType());

        event.setCreationTime(LocalDateTime.now());
        if (EventType.VIRTUAL.equals(event.getEventType())) {
            // event.setAddress(null);
            event.setAddressId(null);
        } else {
            Address address = getAddressById(event.getAddressId());
            if (address == null) {
                throw new IllegalStateException("Invalid address ID: " + event.getAddressId());
            }
            event.setAddressId(address.getAddressId());
        }

        // event.setAddress(address);
        // && !event.getSpeakers().isEmpty()
        if (event.getSpeakerIds() != null && !event.getSpeakerIds().isEmpty()) {
            // List<Integer> speakerIds = event.getSpeakerIds().stream()
            //         .map(Integer::getSpeakerId)
            //         .collect(Collectors.toList());
            List<Integer> speakerIds = event.getSpeakerIds();
            List<Speaker> speakers = new ArrayList<>();
            List<Integer> speakerIds2 = new ArrayList<>();
            for (Integer speakerId : speakerIds) {
                Speaker speaker = getSpeakerById(speakerId);
                // .orElseThrow(() -> new RuntimeException("Speaker not found: " + speakerId));
                speakers.add(speaker);
                speakerIds2.add(speakerId);
            }
            event.setSpeakerIds(speakerIds2);
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
        // return eventRepository.save(event);
        Event savedEvent = eventRepository.save(event);
        return getDTO(savedEvent);
    }

    @Override
    public EventDTO getEventById(int id) {
        // return eventRepository.findById(id)
                // .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        return getDTO(event);
    }

    @Override
    public List<EventDTO> getAllEvents() {
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

        // return upcomingEvents;
        return upcomingEvents.stream()
                .map(this::getDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EventDTO updateEvent(int id, Event updatedEvent) {

        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException("Event not found with id: " + id);
        }
        // Event existing = getEventById(id);
        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        existing.setEventName(updatedEvent.getEventName());
        existing.setDescription(updatedEvent.getDescription());
        existing.setDate(updatedEvent.getDate());
        existing.setEventType(updatedEvent.getEventType());
        existing.setImage(updatedEvent.getImage());
        existing.setMaxAttendees(updatedEvent.getMaxAttendees());

        if (EventType.VIRTUAL.equals(updatedEvent.getEventType())) {
            // existing.setAddress(null);
            existing.setAddressId(null);
        } else {
            Address address = getAddressById(updatedEvent.getAddressId());
            if (address == null) {
                throw new IllegalStateException("Invalid address ID: " + updatedEvent.getAddressId());
            }
            existing.setAddressId(address.getAddressId());
        }

        if (updatedEvent.getSpeakerIds() != null &&
                !updatedEvent.getSpeakerIds().isEmpty()) {
            List<Integer> speakerIds = updatedEvent.getSpeakerIds();
            List<Speaker> speakers = getAllSpeakers();
            if (speakers.size() != speakerIds.size()) {
                throw new IllegalStateException("One or more speaker IDs are invalid.");
            }
            // existing.setSpeakerIds(speakers);
            List<Integer> speakerId = new ArrayList<>();
            for (Speaker speaker : speakers) {
                speakerId.add(speaker.getSpeakerId());
            }
            existing.setSpeakerIds(speakerId);
        } else {
            existing.setSpeakerIds(List.of());
        }
        existing.setActiveStatus(updatedEvent.getActiveStatus());

        existing.setUpdatedOn(LocalDateTime.now().toLocalDate());
        // return eventRepository.save(existing);
        Event savedEvent = eventRepository.save(existing);
        return getDTO(savedEvent);

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
    public List<EventDTO> findByEventName(String eventName) {
        List<Event> event = eventRepository.findByEventName(eventName);
        if (event == null) {
            throw new EventNotFoundException("Event not found with name: " + eventName);
        }
        // return event;
        return event.stream()
                .map(this::getDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<EventDTO> getPaginatedEvents(String eventName, Pageable pageable) {
        EventStatus active = EventStatus.ACTIVE;

        if (eventName != null && !eventName.isEmpty()) {
            // return eventRepository.findByEventNameContainingIgnoreCaseAndActiveStatusOrderByCreationTimeDesc(eventName,
                    // active, pageable);
            return eventRepository.findByEventNameContainingIgnoreCaseAndActiveStatusOrderByCreationTimeDesc(eventName,
                    active, pageable).map(this::getDTO);
        }

        // return eventRepository.findByActiveStatusOrderByCreationTimeDesc(active, pageable);
        return eventRepository.findByActiveStatusOrderByCreationTimeDesc(active, pageable).map(this::getDTO);
    }

}