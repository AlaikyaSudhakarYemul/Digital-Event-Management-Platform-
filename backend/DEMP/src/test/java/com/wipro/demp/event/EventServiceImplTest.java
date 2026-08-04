package com.wipro.demp.event;

import com.wipro.demp.entity.Address;
import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.EventStatus;
import com.wipro.demp.entity.EventType;
import com.wipro.demp.entity.Speaker;
import com.wipro.demp.entity.Users;
import com.wipro.demp.exception.AddressNotFoundException;
import com.wipro.demp.exception.EventNotFoundException;
import com.wipro.demp.repository.EventRepository;
import com.wipro.demp.repository.SpeakerRepository;
import com.wipro.demp.repository.UserRepository;
import com.wipro.demp.service.AddressService;
import com.wipro.demp.service.EventServiceImpl;
import com.wipro.demp.service.SpeakerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventServiceImplTest {

    private EventRepository eventRepository;
    private AddressService addressService;
    private SpeakerService speakerService;
    private SpeakerRepository speakerRepository;
    private UserRepository userRepository;
    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventRepository = Mockito.mock(EventRepository.class);
        addressService = Mockito.mock(AddressService.class);
        speakerService = Mockito.mock(SpeakerService.class);
        speakerRepository = Mockito.mock(SpeakerRepository.class);
        userRepository = Mockito.mock(UserRepository.class);

        eventService = new EventServiceImpl(eventRepository, addressService, speakerService, speakerRepository, userRepository);
    }

    private Event buildEvent(EventType type) {
        Event event = new Event();
        event.setEventId(1);
        event.setEventName("Tech Summit");
        event.setDescription("A valid event description");
        event.setDate(LocalDate.now().plusDays(5));
        event.setEventType(type);
        event.setMaxAttendees(100);

        Users user = new Users();
        user.setUserId(7);
        event.setUser(user);

        Address address = new Address();
        address.setAddressId(12);
        event.setAddress(address);

        Speaker speaker = new Speaker();
        speaker.setSpeakerId(20);
        event.setSpeakers(List.of(speaker));

        return event;
    }

    @Test
    void createEventVirtualSkipsAddressLookup() {
        Event event = buildEvent(EventType.VIRTUAL);
        Users user = new Users();
        user.setUserId(7);

        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(speakerRepository.findAllById(List.of(20))).thenReturn(event.getSpeakers());
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event saved = eventService.createEvent(event);

        assertNotNull(saved.getCreationTime());
        assertEquals(EventStatus.ACTIVE, saved.getActiveStatus());
        assertNull(saved.getAddress());
        verify(addressService, never()).getAddress(any(Integer.class));
    }

    @Test
    void createEventPhysicalWithMissingAddressThrows() {
        Event event = buildEvent(EventType.IN_PERSON);
        when(addressService.getAddress(12)).thenReturn(null);

        AddressNotFoundException ex = assertThrows(AddressNotFoundException.class, () -> eventService.createEvent(event));
        assertTrue(ex.getMessage().contains("Invalid address ID"));
    }

    @Test
    void createEventWithAddressAndUserAndSpeakersSuccess() {
        Event event = buildEvent(EventType.IN_PERSON);
        Address resolvedAddress = new Address();
        resolvedAddress.setAddressId(12);
        Users resolvedUser = new Users();
        resolvedUser.setUserId(7);

        when(addressService.getAddress(12)).thenReturn(resolvedAddress);
        when(userRepository.findById(7)).thenReturn(Optional.of(resolvedUser));
        when(speakerRepository.findAllById(List.of(20))).thenReturn(event.getSpeakers());
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event saved = eventService.createEvent(event);

        assertEquals(12, saved.getAddress().getAddressId());
        assertEquals(7, saved.getUser().getUserId());
        assertEquals(EventStatus.ACTIVE, saved.getActiveStatus());
        assertNotNull(saved.getCreatedOn());
        assertNotNull(saved.getUpdatedOn());
        assertTrue(!saved.isDeleted());
    }

    @Test
    void findAllEventsByUserIdEmptyThrows() {
        when(eventRepository.findByUserUserId(99)).thenReturn(List.of());

        assertThrows(EventNotFoundException.class, () -> eventService.findAllEventsByUserId(99));
    }

    @Test
    void getAllEventsFiltersDeletedAndCompletesPastEvents() {
        Event pastActive = buildEvent(EventType.VIRTUAL);
        pastActive.setDate(LocalDate.now().minusDays(1));
        pastActive.setActiveStatus(EventStatus.ACTIVE);

        Event deleted = buildEvent(EventType.VIRTUAL);
        deleted.setEventId(2);
        deleted.setActiveStatus(EventStatus.DELETED);

        when(eventRepository.findAllInReverse()).thenReturn(List.of(pastActive, deleted));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Event> events = eventService.getAllEvents();

        assertEquals(1, events.size());
        assertEquals(EventStatus.COMPLETED, events.get(0).getActiveStatus());
        verify(eventRepository).save(pastActive);
    }

    @Test
    void updateEventVirtualClearsAddressAndEmptySpeakers() {
        Event existing = buildEvent(EventType.IN_PERSON);
        Event updated = buildEvent(EventType.VIRTUAL);
        updated.setSpeakers(List.of());
        updated.setActiveStatus(EventStatus.ACTIVE);

        when(eventRepository.existsById(1)).thenReturn(true);
        when(eventRepository.findById(1)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.updateEvent(1, updated);

        assertNull(result.getAddress());
        assertEquals(0, result.getSpeakers().size());
        assertNotNull(result.getUpdatedOn());
    }

    @Test
    void updateEventInvalidSpeakerListThrows() {
        Event existing = buildEvent(EventType.IN_PERSON);
        Event updated = buildEvent(EventType.IN_PERSON);
        updated.setSpeakers(List.of(new Speaker(), new Speaker()));
        updated.getSpeakers().get(0).setSpeakerId(1);
        updated.getSpeakers().get(1).setSpeakerId(2);

        Address resolvedAddress = new Address();
        resolvedAddress.setAddressId(12);

        when(eventRepository.existsById(1)).thenReturn(true);
        when(eventRepository.findById(1)).thenReturn(Optional.of(existing));
        when(addressService.getAddress(12)).thenReturn(resolvedAddress);
        when(speakerService.findAllByIds(List.of(1, 2))).thenReturn(List.of(updated.getSpeakers().get(0)));

        assertThrows(IllegalStateException.class, () -> eventService.updateEvent(1, updated));
    }

    @Test
    void deleteEventMarksAsDeleted() {
        Event existing = buildEvent(EventType.VIRTUAL);

        when(eventRepository.existsById(1)).thenReturn(true);
        when(eventRepository.findById(1)).thenReturn(Optional.of(existing));

        eventService.deleteEvent(1);

        assertEquals(EventStatus.DELETED, existing.getActiveStatus());
        verify(eventRepository).save(existing);
    }

    @Test
    void getPaginatedEventsUsesNameFilterWhenProvided() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Event> page = new PageImpl<>(List.of(buildEvent(EventType.VIRTUAL)));

        when(eventRepository.findByEventNameContainingIgnoreCaseAndActiveStatusOrderByCreationTimeDesc(eq("Tech"), eq(EventStatus.ACTIVE), eq(pageable)))
                .thenReturn(page);

        Page<Event> result = eventService.getPaginatedEvents("Tech", pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getPaginatedEventsWithoutNameUsesActiveOnly() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Event> page = new PageImpl<>(List.of(buildEvent(EventType.VIRTUAL)));

        when(eventRepository.findByActiveStatusOrderByCreationTimeDesc(EventStatus.ACTIVE, pageable)).thenReturn(page);

        Page<Event> result = eventService.getPaginatedEvents("", pageable);

        assertEquals(1, result.getContent().size());
    }
}
