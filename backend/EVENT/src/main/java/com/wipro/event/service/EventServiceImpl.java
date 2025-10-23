package com.wipro.event.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.wipro.event.dto.EventDTO;
import com.wipro.event.entity.Address;
import com.wipro.event.entity.Event;
import com.wipro.event.entity.EventStatus;
import com.wipro.event.entity.EventType;
import com.wipro.event.entity.Speaker;
import com.wipro.event.entity.Users;
import com.wipro.event.exception.EventNotFoundException;
import com.wipro.event.repository.EventRepository;


@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    // private final UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    // public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
    //     this.eventRepository = eventRepository;
    //     this.userRepository = userRepository;
    // }
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Address getAddressById(int addressId) {
        String url = "http://ADMIN/api/admin/" + addressId;
        return restTemplate.getForObject(url, Address.class);
    }

    public Speaker getSpeakerById(int speakerId) {
        String url = "http://ADMIN/api/speakers/" + speakerId;
        return restTemplate.getForObject(url, Speaker.class);
    }

    public List<Speaker> getAllSpeakers() {
        String url = "http://ADMIN/api/speakers";
        Speaker[] speakersArray = restTemplate.getForObject(url, Speaker[].class);
        if (speakersArray == null) {
            return new ArrayList<>();
        }
        return List.of(speakersArray);
    }

    public Users getUserById(int userId){
        String url = "http://DEMP/api/user/" + userId;
        // Extract JWT token from current request
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new IllegalStateException("No request context available for forwarding JWT token");
        }
        jakarta.servlet.http.HttpServletRequest request = (jakarta.servlet.http.HttpServletRequest) ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getRequest();
        String authHeader = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null) {
            headers.set("Authorization", authHeader);
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Users> response = restTemplate.exchange(url, HttpMethod.GET, entity, Users.class);
        return response.getBody();
    }

    public EventDTO getDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setEventId(event.getEventId());
        dto.setEventName(event.getEventName());
        dto.setDescription(event.getDescription());
        dto.setDate(event.getDate());
        dto.setTime(event.getTime());
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
        if(event.getUserId()!=null){
            Users user = getUserById(event.getUserId());
            dto.setUser(user);
        }
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

    /*public Event setDTO(EventDTO dto) {
        Event event = new Event();
        event.setEventId(dto.getEventId());
        event.setEventName(dto.getEventName());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setImage(dto.getImage());
        event.setEventType(dto.getEventType());
        event.setAddressId(dto.getAddressId());
        event.setSpeakerIds(dto.getSpeakerIds());
        event.setUserId(dto.getUser().getUserId());
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
    }*/

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

        // Users createdBy = userRepository.findById(event.getUser().getUserId())
        //         .orElseThrow(
        //                 () -> new EventNotFoundException("User not found with id: " + event.getUser().getUserId()));

        // event.setUser(createdBy);

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
        	    .filter(event -> event.getDate() == null || !event.getDate().isBefore(today))
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

        if (updatedEvent.getSpeakerIds() != null && !updatedEvent.getSpeakerIds().isEmpty()) {
            List<Integer> speakerIds = updatedEvent.getSpeakerIds();
            List<Integer> validSpeakerIds = new ArrayList<>();
            for (Integer speakerId : speakerIds) {
                Speaker speaker = getSpeakerById(speakerId);
                if (speaker == null) {
                    throw new IllegalStateException("Speaker ID " + speakerId + " is invalid.");
                }
                validSpeakerIds.add(speakerId);
            }
            existing.setSpeakerIds(validSpeakerIds);
        } else {
            existing.setSpeakerIds(List.of());
        }
        if(updatedEvent.getActiveStatus()!=null){
            existing.setActiveStatus(updatedEvent.getActiveStatus());
        }        

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
    public List<EventDTO> findEventsByUserId(int userId){
        List<Event> events = eventRepository.findByUserId(userId);
        if (events == null || events.isEmpty()) {
            throw new EventNotFoundException("No events found for userId: " + userId);
        }
        return events.stream()
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