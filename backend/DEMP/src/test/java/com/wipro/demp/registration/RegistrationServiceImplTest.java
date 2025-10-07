package com.wipro.demp.registration;



import com.wipro.demp.entity.*;
import com .wipro.demp.entity.RegistrationStatus;
import com.wipro.demp.repository.*;
import com.wipro.demp.service.RegistrationServiceImpl;
import com.wipro.demp.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceImplTest {

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateRegistrationSuccess() {
        Users user = new Users();
        user.setUserId(1);
        user.setUserName("Alice");

        Event event = new Event();
        event.setEventId(100);
        event.setEventName("TechConf");

        Registrations registration = new Registrations();
        registration.setUser(user);
        registration.setEvent(event);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(eventRepository.findById(100)).thenReturn(Optional.of(event));
        when(registrationRepository.save(any(Registrations.class))).thenAnswer(i -> i.getArgument(0));

        Registrations result = registrationService.createRegistration(registration);

        assertNotNull(result.getCreatedOn());
        assertEquals(RegistrationStatus.REGISTERED, result.getStatus());
        verify(registrationRepository).save(any());
    }

    @Test
    void testGetRegistrationByIdFound() {
        Registrations registration = new Registrations();
        registration.setRegistrationId(5);
        when(registrationRepository.findById(5)).thenReturn(Optional.of(registration));

        Registrations found = registrationService.getRegistrationById(5);
        assertEquals(5, found.getRegistrationId());
    }

    @Test
    void testUpdateRegistrationSuccess() {
        Users user = new Users();
        user.setUserId(1);

        Event event = new Event();
        event.setEventId(2);

        Registrations existing = new Registrations();
        existing.setRegistrationId(10);
        existing.setCreatedOn(LocalDate.now().minusDays(1));

        Registrations updated = new Registrations();
        updated.setRegistrationId(10);
        updated.setUser(user);
        updated.setEvent(event);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(eventRepository.findById(2)).thenReturn(Optional.of(event));
        when(registrationRepository.findById(10)).thenReturn(Optional.of(existing));
        when(registrationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Registrations result = registrationService.updateRegistration(updated);
        assertEquals(RegistrationStatus.UPDATED, result.getStatus());
    }

    @Test
    void testDeleteRegistrationSuccess() {
        Registrations reg = new Registrations();
        reg.setRegistrationId(7);

        when(registrationRepository.findById(7)).thenReturn(Optional.of(reg));

        registrationService.deleteRegistration(7);
        assertTrue(reg.isDeleted());
        assertEquals(RegistrationStatus.CANCELLED, reg.getStatus());
        verify(registrationRepository).save(reg);
    }
}