package com.wipro.demp.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.wipro.demp.entity.*;
import com.wipro.demp.repository.*;
import com.wipro.demp.service.AddressService;
import com.wipro.demp.service.EventServiceImpl;
import com.wipro.demp.exception.*;

class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    
    @Mock
    private AddressService addressService;
    
    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;
    private Address address;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        address = new Address();
        address.setAddressId(101);
        address.setAddress("New York");

        event = new Event();
        event.setEventId(1);
        event.setEventName("Tech Summit");
        event.setDescription("Annual Tech Conference");
        event.setDate(LocalDate.now());
        event.setEventType(EventType.IN_PERSON); // Set enum directly
        event.setAddress(address);
        event.setCreationTime(LocalDateTime.now());
        event.setActiveStatus(EventStatus.ACTIVE);
    }

    @Test
    void testCreateEvent() {
        when(addressService.getAddress(address.getAddressId())).thenReturn(address);
        when(eventRepository.save(event)).thenReturn(event);

        Event createdEvent = eventService.createEvent(event);

        assertNotNull(createdEvent);
        assertEquals("Tech Summit", createdEvent.getEventName());
    }

    @Test
    void testCreateEvent_AddressNotFound() {
        when(addressService.getAddress(address.getAddressId())).thenReturn(null);

        assertThrows(AddressNotFoundException.class, () -> eventService.createEvent(event));
    }

    @Test
    void testGetEventById() {
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        Event foundEvent = eventService.getEventById(1);

        assertNotNull(foundEvent);
        assertEquals(1, foundEvent.getEventId());
    }

    @Test
    void testGetEventById_NotFound() {
        when(eventRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.getEventById(1));
    }

    @Test
    void testUpdateEvent() {
        when(eventRepository.existsById(1)).thenReturn(true);
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(addressService.getAddress(address.getAddressId())).thenReturn(address);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event updatedEvent = new Event();
        updatedEvent.setEventId(1);
        updatedEvent.setEventName("Updated Tech Summit");
        updatedEvent.setDescription("Updated Description");
        updatedEvent.setDate(LocalDate.now());
        updatedEvent.setEventType(EventType.IN_PERSON); // Set enum directly
        updatedEvent.setAddress(address);

        Event result = eventService.updateEvent(1, updatedEvent);

        assertNotNull(result);
        assertEquals("Updated Tech Summit", result.getEventName());
    }

    @Test
    void testDeleteEvent() {
        when(eventRepository.existsById(1)).thenReturn(true);

        eventService.deleteEvent(1);

        verify(eventRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteEvent_NotFound() {
        when(eventRepository.existsById(1)).thenReturn(false);

        assertThrows(EventNotFoundException.class, () -> eventService.deleteEvent(1));
    }

    @Test
    void testFindByEventName() {
        when(eventRepository.findByEventName("Tech Summit")).thenReturn(Arrays.asList(event));

        List<Event> events = eventService.findByEventName("Tech Summit");

        assertFalse(events.isEmpty());
        assertEquals("Tech Summit", events.get(0).getEventName());
    }
}