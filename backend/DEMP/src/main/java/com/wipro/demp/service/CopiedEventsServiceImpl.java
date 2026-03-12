package com.wipro.demp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
 
import com.wipro.demp.entity.*;

import com.wipro.demp.repository.*;
import com.wipro.demp.exception.*;

@Service
public class CopiedEventsServiceImpl implements CopiedEventsService {

    private final CopiedEventsRepository copiedEventsRepository;
    private final AddressService addressService;
    private final SpeakerService speakerService;
    private final SpeakerRepository speakerRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public CopiedEventsServiceImpl(CopiedEventsRepository copiedEventsRepository, AddressService addressService,
            SpeakerService speakerService, SpeakerRepository speakerRepository, UserRepository userRepository,
            EventRepository eventRepository) {
        this.copiedEventsRepository = copiedEventsRepository;
        this.addressService = addressService;
        this.speakerService = speakerService;
        this.speakerRepository = speakerRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public CopiedEvents createCopiedEvents(CopiedEvents copiedEvents) {

        Integer sourceEventId = copiedEvents.getEventId();
        if (sourceEventId == null || sourceEventId <= 0) {
            throw new CopiedEventsNotFoundException("Invalid source event id: " + sourceEventId);
        }
        Event sourceEvent = eventRepository.findById(sourceEventId)
                .orElseThrow(() -> new CopiedEventsNotFoundException("Source event not found with id: " + sourceEventId));
        copiedEvents.setEvent(sourceEvent);

        copiedEvents.setEventType(copiedEvents.getEventType());

        copiedEvents.setCreationTime(LocalDateTime.now());
        if (EventType.VIRTUAL.equals(copiedEvents.getEventType())) {
            copiedEvents.setAddress(null);
        } else {
            Address address = addressService.getAddress(copiedEvents.getAddress().getAddressId());
            if (address == null) {
                throw new AddressNotFoundException("Invalid address ID: " + copiedEvents.getAddress().getAddressId());
            }
            copiedEvents.setAddress(address);
        }

        if (copiedEvents.getSpeakers() != null && !copiedEvents.getSpeakers().isEmpty()) {
            List<Integer> speakerIds = copiedEvents.getSpeakers().stream()
                    .map(Speaker::getSpeakerId)
                    .collect(Collectors.toList());
            List<Speaker> speakers = speakerRepository.findAllById(speakerIds);
            
            copiedEvents.setSpeakers(speakers);
        }

        Users createdBy = userRepository.findById(copiedEvents.getUser().getUserId())
                .orElseThrow(
                        () -> new CopiedEventsNotFoundException("User not found with id: " + copiedEvents.getUser().getUserId()));

        copiedEvents.setUser(createdBy);
        copiedEvents.setActiveStatus(EventStatus.ACTIVE);
        copiedEvents.setActiveStatus(EventStatus.ACTIVE);
        copiedEvents.setMaxAttendees(copiedEvents.getMaxAttendees());
        copiedEvents.setCreatedOn(LocalDateTime.now().toLocalDate());
        copiedEvents.setCreationTime(LocalDateTime.now());
        copiedEvents.setUpdatedOn(LocalDateTime.now().toLocalDate());
        copiedEvents.setDeleted(false);
        return copiedEventsRepository.save((CopiedEvents) copiedEvents);
    }

    @Override
    public CopiedEvents getCopiedEventsById(int id) {
        Optional<CopiedEvents> copiedEvent = copiedEventsRepository.findById(id);
        return copiedEvent.orElseThrow(() -> new CopiedEventsNotFoundException("CopiedEvents not found with id: " + id));
    }

    @Override
    public List<CopiedEvents> getAllCopiedEvents() {
        List<CopiedEvents> events = copiedEventsRepository.findAll().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getCopiedEventId(), e1.getCopiedEventId()))
                .collect(Collectors.toList());
        if (events.isEmpty()) {
            throw new CopiedEventsNotFoundException("No events found.");
        }

        LocalDate today = LocalDate.now();

        
        List<CopiedEvents> upcomingEvents = events.stream()
                .filter(event -> {
                    if (event.getDate() != null && event.getDate().isBefore(today)) {
                        event.setActiveStatus(EventStatus.COMPLETED);
                        copiedEventsRepository.save(event);
                        return false; 
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return upcomingEvents;
    }

    @Override
    public CopiedEvents updateCopiedEvents(int id, CopiedEvents updatedCopiedEvents) {

        if (!copiedEventsRepository.existsById(id)) {
            throw new CopiedEventsNotFoundException("CopiedEvents not found with id: " + id);
        }
        CopiedEvents existing = getCopiedEventsById(id);
        existing.setEventName(updatedCopiedEvents.getEventName());
        existing.setDescription(updatedCopiedEvents.getDescription());
        existing.setDate(updatedCopiedEvents.getDate());
        existing.setTime(updatedCopiedEvents.getTime());
        existing.setEventType(updatedCopiedEvents.getEventType());
        existing.setImage(updatedCopiedEvents.getImage());
        existing.setMaxAttendees(updatedCopiedEvents.getMaxAttendees());

        Integer sourceEventId = updatedCopiedEvents.getEventId();
        if (sourceEventId != null && sourceEventId > 0) {
            Event sourceEvent = eventRepository.findById(sourceEventId)
                .orElseThrow(() -> new CopiedEventsNotFoundException("Source event not found with id: " + sourceEventId));
            existing.setEvent(sourceEvent);
        }

        if (EventType.VIRTUAL.equals(updatedCopiedEvents.getEventType())) {
            existing.setAddress(null);
        } else {
            Address address = addressService.getAddress(updatedCopiedEvents.getAddress().getAddressId());
            if (address == null) {
                throw new AddressNotFoundException("Invalid address ID: " + updatedCopiedEvents.getAddress().getAddressId());
            }
            existing.setAddress(address);
        }

        if (updatedCopiedEvents.getSpeakers() != null &&
                !updatedCopiedEvents.getSpeakers().isEmpty()) {
            List<Integer> speakerIds = updatedCopiedEvents.getSpeakers().stream()
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
        existing.setActiveStatus(updatedCopiedEvents.getActiveStatus());

        existing.setUpdatedOn(LocalDateTime.now().toLocalDate());
        return copiedEventsRepository.save(existing);

    }

    @Override
    public void deleteCopiedEvents(int id) {
        if (copiedEventsRepository.existsById(id) == false) {
            throw new CopiedEventsNotFoundException("CopiedEvents not found with id: " + id);
        }
        Optional<CopiedEvents> copiedEvent = copiedEventsRepository.findById(id);
        if (copiedEvent.isPresent()) {
            CopiedEvents existingCopiedEvent = copiedEvent.get();
            existingCopiedEvent.setActiveStatus(EventStatus.DELETED);
            existingCopiedEvent.setUpdatedOn(LocalDateTime.now().toLocalDate());
            existingCopiedEvent.setCreationTime(LocalDateTime.now());
            copiedEventsRepository.save(existingCopiedEvent);
        }
    }

    @Override
    public List<CopiedEvents> findByCopiedEventsName(String eventName) {
        List<CopiedEvents> copiedEvents = copiedEventsRepository.findByEventName(eventName);
        if (copiedEvents == null) {
            throw new CopiedEventsNotFoundException("CopiedEvents not found with name: " + eventName);
        }
        return copiedEvents;
    }

}


    

